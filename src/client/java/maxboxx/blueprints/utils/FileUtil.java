package maxboxx.blueprints.utils;

import maxboxx.blueprints.SimpleBlueprints;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FileUtil {
	private static final ExecutorService DIALOG_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "Dialog-Thread");
		t.setDaemon(true);
		return t;
	});

	private static final AtomicBoolean DIALOG_STATE = new AtomicBoolean(false);

	public static void openFileDialogAsync(String title, String[] filters, Consumer<String> callback) {
		showFileDialogAsync(DialogType.OPEN, title, filters, callback);
	}

	public static void saveFileDialogAsync(String title, String[] filters, Consumer<String> callback) {
		showFileDialogAsync(DialogType.SAVE, title, filters, callback);
	}

	private static void showFileDialogAsync(DialogType type, String title, String[] filters, Consumer<String> callback) {
		if (!DIALOG_STATE.compareAndSet(false, true)) {
			return;
		}

		try {
			DIALOG_EXECUTOR.submit(() -> {
				try {
					Path startingDirectory = getStartingDirectory();
					DialogResult result = isLinux()
						? runLinuxDialog(type, title, startingDirectory, filters)
						: runTinyFileDialog(type, title, startingDirectory, filters);

					if (result.status() == DialogStatus.SELECTED) {
						dispatchSuccess(result.path(), callback);
					}
				}
				catch (Throwable throwable) {
					SimpleBlueprints.LOGGER.error("Unexpected file dialog failure", throwable);
				}
				finally {
					DIALOG_STATE.set(false);
				}
			});
		}
		catch (RejectedExecutionException exception) {
			DIALOG_STATE.set(false);
			SimpleBlueprints.LOGGER.error("File dialog executor rejected the request", exception);
		}
	}

	private static DialogResult runLinuxDialog(DialogType type, String title, Path startingDirectory, String[] filters) {
		DialogResult result = runKDialog(type, title, startingDirectory, filters);
		if (result.status() != DialogStatus.FAILED || Thread.currentThread().isInterrupted()) {
			return result;
		}

		SimpleBlueprints.LOGGER.info("Falling back to zenity");
		result = runZenity(type, title, startingDirectory, filters);
		if (result.status() != DialogStatus.FAILED || Thread.currentThread().isInterrupted()) {
			return result;
		}

		SimpleBlueprints.LOGGER.info("Falling back to TinyFileDialogs");
		result = runTinyFileDialog(type, title, startingDirectory, filters);
		if (result.status() == DialogStatus.FAILED) {
			SimpleBlueprints.LOGGER.error("All file-dialog backends failed");
		}

		return result;
	}

	private static DialogResult runKDialog(DialogType type, String title, Path startingDirectory, String[] filters) {
		List<String> command = new ArrayList<>();
		command.add("kdialog");
		command.add("--title");
		command.add(title);
		command.add(type == DialogType.OPEN ? "--getopenfilename" : "--getsavefilename");
		command.add(startingDirectory.toString());
		command.add(toKDialogFilter(filters));
		return runExternalDialog("kdialog", command);
	}

	private static DialogResult runZenity(DialogType type, String title, Path startingDirectory, String[] filters) {
		List<String> command = new ArrayList<>();
		command.add("zenity");
		command.add("--file-selection");
		if (type == DialogType.SAVE) {
			command.add("--save");
			command.add("--confirm-overwrite");
		}
		command.add("--title");
		command.add(title);
		command.add("--filename");
		command.add(startingDirectory + File.separator);
		command.add("--file-filter");
		command.add(toZenityFilter(filters));
		return runExternalDialog("zenity", command);
	}

	private static DialogResult runExternalDialog(String backend, List<String> command) {
		SimpleBlueprints.LOGGER.info("Trying file dialog backend: {}", backend);

		try {
			ProcessResult result = runDialogProcess(command);
			if (result.exitCode() == 0) {
				return result.stdout().isEmpty() ? DialogResult.cancelled() : DialogResult.selected(result.stdout());
			}
			if (result.exitCode() == 1) {
				return DialogResult.cancelled();
			}

			SimpleBlueprints.LOGGER.warn("{} failed with exit code {}", backend, result.exitCode());
			if (!result.stderr().isEmpty()) {
				SimpleBlueprints.LOGGER.warn("{} stderr: {}", backend, result.stderr());
			}
			return DialogResult.failed();
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			SimpleBlueprints.LOGGER.warn("{} file dialog was interrupted", backend);
			return DialogResult.failed();
		}
		catch (IOException exception) {
			SimpleBlueprints.LOGGER.info("{} unavailable or could not start: {}", backend, exception.getMessage());
			return DialogResult.failed();
		}
	}

	private static ProcessResult runDialogProcess(List<String> command) throws IOException, InterruptedException {
		Path stdoutFile = Files.createTempFile("simple-blueprints-dialog-", ".out");
		Path stderrFile = null;
		Process process = null;

		try {
			stderrFile = Files.createTempFile("simple-blueprints-dialog-", ".err");
			process = new ProcessBuilder(command)
				.redirectOutput(stdoutFile.toFile())
				.redirectError(stderrFile.toFile())
				.start();

			int exitCode;
			try {
				exitCode = process.waitFor();
			}
			catch (InterruptedException exception) {
				destroyProcess(process);
				throw exception;
			}

			String stdout = stripTrailingLineTerminators(Files.readString(stdoutFile, StandardCharsets.UTF_8));
			String stderr = stripTrailingLineTerminators(Files.readString(stderrFile, StandardCharsets.UTF_8));
			return new ProcessResult(exitCode, stdout, stderr);
		}
		finally {
			if (process != null && process.isAlive()) {
				destroyProcess(process);
			}
			deleteTempFile(stdoutFile);
			if (stderrFile != null) {
				deleteTempFile(stderrFile);
			}
		}
	}

	private static void destroyProcess(Process process) {
		process.destroy();
		try {
			if (!process.waitFor(1, TimeUnit.SECONDS)) {
				process.destroyForcibly();
			}
		}
		catch (InterruptedException exception) {
			process.destroyForcibly();
			Thread.currentThread().interrupt();
		}
	}

	private static void deleteTempFile(Path file) {
		try {
			Files.deleteIfExists(file);
		}
		catch (IOException exception) {
			SimpleBlueprints.LOGGER.debug("Failed to delete file dialog temporary output", exception);
		}
	}

	private static DialogResult runTinyFileDialog(DialogType type, String title, Path startingDirectory, String[] filters) {
		SimpleBlueprints.LOGGER.info("Trying file dialog backend: TinyFileDialogs");

		try (MemoryStack stack = MemoryStack.stackPush()) {
			PointerBuffer buffer = toPointerBuffer(stack, filters);
			String initialPath = startingDirectory + File.separator;
			String path = type == DialogType.OPEN
				? TinyFileDialogs.tinyfd_openFileDialog(title, initialPath, buffer, null, false)
				: TinyFileDialogs.tinyfd_saveFileDialog(title, initialPath, buffer, null);

			return path == null || path.isEmpty() ? DialogResult.cancelled() : DialogResult.selected(path);
		}
		catch (Throwable throwable) {
			SimpleBlueprints.LOGGER.error("TinyFileDialogs failed", throwable);
			return DialogResult.failed();
		}
	}

	private static void dispatchSuccess(String path, Consumer<String> callback) {
		Minecraft.getInstance().execute(() -> {
			try {
				callback.accept(path);
			}
			catch (Throwable throwable) {
				SimpleBlueprints.LOGGER.error("File dialog callback failed", throwable);
			}
		});
	}

	private static Path getStartingDirectory() {
		Path gameDirectory = FabricLoader.getInstance().getGameDir();
		if (Files.isDirectory(gameDirectory)) {
			return gameDirectory;
		}

		String userHome = System.getProperty("user.home");
		if (userHome != null && !userHome.isEmpty()) {
			try {
				Path homeDirectory = Path.of(userHome);
				if (Files.isDirectory(homeDirectory)) {
					return homeDirectory;
				}
			}
			catch (RuntimeException ignored) {
				// Fall back to the process working directory.
			}
		}

		return Path.of(".").toAbsolutePath().normalize();
	}

	private static boolean isLinux() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
	}

	private static String toKDialogFilter(String[] filters) {
		return "Supported files (" + String.join(" ", filters) + ")";
	}

	private static String toZenityFilter(String[] filters) {
		return "Supported files | " + String.join(" ", filters);
	}

	private static String stripTrailingLineTerminators(String value) {
		int end = value.length();
		while (end > 0) {
			char character = value.charAt(end - 1);
			if (character != '\n' && character != '\r') {
				break;
			}
			end--;
		}
		return value.substring(0, end);
	}

	private static PointerBuffer toPointerBuffer(MemoryStack stack, String[] strings) {
		return toPointerBuffer(stack, strings, false);
	}

	private static PointerBuffer toPointerBuffer(MemoryStack stack, String[] strings, boolean nullTerminate) {
		PointerBuffer buffer = stack.mallocPointer(nullTerminate ? strings.length + 1 : strings.length);

		for (String s : strings) {
			ByteBuffer utf8 = stack.UTF8(s, true);
			buffer.put(MemoryUtil.memAddress(utf8));
		}

		if (nullTerminate) {
			buffer.put(0);
		}

		buffer.flip();
		return buffer;
	}

	private enum DialogType {
		OPEN,
		SAVE
	}

	private enum DialogStatus {
		SELECTED,
		CANCELLED,
		FAILED
	}

	private record DialogResult(DialogStatus status, String path) {
		private static DialogResult selected(String path) {
			return new DialogResult(DialogStatus.SELECTED, path);
		}

		private static DialogResult cancelled() {
			return new DialogResult(DialogStatus.CANCELLED, null);
		}

		private static DialogResult failed() {
			return new DialogResult(DialogStatus.FAILED, null);
		}
	}

	private record ProcessResult(int exitCode, String stdout, String stderr) {
	}
}
