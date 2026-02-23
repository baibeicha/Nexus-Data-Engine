package by.nexus.core.service.security;

import lombok.extern.slf4j.Slf4j;

import java.lang.ref.Cleaner;
import java.util.Arrays;

/**
 * Контейнер для безопасного хранения расшифрованных данных в памяти.
 * Автоматически очищает память при закрытии или сборке мусора.
 */
@Slf4j
public class SecureMemoryContainer implements AutoCloseable {

    private static final Cleaner cleaner = Cleaner.create();
    private final byte[] data;
    private final Cleaner.Cleanable cleanable;
    private boolean closed = false;

    public SecureMemoryContainer(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
        this.cleanable = cleaner.register(this, new CleaningAction(this.data));
    }

    /**
     * Возвращает копию данных
     */
    public byte[] getData() {
        if (closed) {
            throw new IllegalStateException("Container has been closed");
        }
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Возвращает размер данных
     */
    public int size() {
        if (closed) {
            throw new IllegalStateException("Container has been closed");
        }
        return data.length;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            // Zero out the data
            Arrays.fill(data, (byte) 0);
            cleanable.clean();
            log.debug("SecureMemoryContainer closed and data zeroed out");
        }
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Cleaning action that zeros out the data when the container is garbage collected
     */
    private static class CleaningAction implements Runnable {
        private final byte[] data;

        CleaningAction(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            Arrays.fill(data, (byte) 0);
            log.debug("SecureMemoryContainer data zeroed out by cleaner");
        }
    }
}
