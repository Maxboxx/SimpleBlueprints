package maxboxx.blueprints.utils;

import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
		if (!DIALOG_STATE.compareAndSet(false, true)) {
			return;
		}

		DIALOG_EXECUTOR.submit(() -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				PointerBuffer buffer = toPointerBuffer(stack, filters);

				String path = TinyFileDialogs.tinyfd_openFileDialog(title, "", buffer, null, false);

				Minecraft.getInstance().execute(() -> {
					if (path == null) return;
					callback.accept(path);
				});
			}
			finally {
				DIALOG_STATE.set(false);
			}
		});
	}

	public static void saveFileDialogAsync(String title, String[] filters, Consumer<String> callback) {
		if (!DIALOG_STATE.compareAndSet(false, true)) {
			return;
		}

		DIALOG_EXECUTOR.submit(() -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				PointerBuffer buffer = toPointerBuffer(stack, filters);

				String path = TinyFileDialogs.tinyfd_saveFileDialog(title, "", buffer, null);

				Minecraft.getInstance().execute(() -> {
					if (path == null) return;
					callback.accept(path);
				});
			}
			finally {
				DIALOG_STATE.set(false);
			}
		});
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
}
