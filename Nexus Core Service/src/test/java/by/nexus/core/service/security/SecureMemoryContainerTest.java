package by.nexus.core.service.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecureMemoryContainerTest {

    @Test
    void getData_ShouldReturnCopyOfData() {
        byte[] original = "test data".getBytes();
        
        try (SecureMemoryContainer container = new SecureMemoryContainer(original)) {
            byte[] retrieved = container.getData();
            
            assertArrayEquals(original, retrieved);
            assertNotSame(original, retrieved); // Should be a copy
        }
    }

    @Test
    void size_ShouldReturnCorrectSize() {
        byte[] data = "test data".getBytes();
        
        try (SecureMemoryContainer container = new SecureMemoryContainer(data)) {
            assertEquals(data.length, container.size());
        }
    }

    @Test
    void close_ShouldMarkAsClosed() {
        byte[] data = "test data".getBytes();
        SecureMemoryContainer container = new SecureMemoryContainer(data);
        
        assertFalse(container.isClosed());
        
        container.close();
        
        assertTrue(container.isClosed());
    }

    @Test
    void getData_AfterClose_ShouldThrowException() {
        byte[] data = "test data".getBytes();
        SecureMemoryContainer container = new SecureMemoryContainer(data);
        
        container.close();
        
        assertThrows(IllegalStateException.class, container::getData);
    }

    @Test
    void size_AfterClose_ShouldThrowException() {
        byte[] data = "test data".getBytes();
        SecureMemoryContainer container = new SecureMemoryContainer(data);
        
        container.close();
        
        assertThrows(IllegalStateException.class, container::size);
    }

    @Test
    void close_MultipleTimes_ShouldNotThrow() {
        byte[] data = "test data".getBytes();
        SecureMemoryContainer container = new SecureMemoryContainer(data);
        
        container.close();
        container.close(); // Should not throw
        
        assertTrue(container.isClosed());
    }

    @Test
    void tryWithResources_ShouldAutoClose() {
        byte[] data = "test data".getBytes();
        
        SecureMemoryContainer container;
        try (SecureMemoryContainer c = new SecureMemoryContainer(data)) {
            container = c;
            assertFalse(c.isClosed());
        }
        
        assertTrue(container.isClosed());
    }

    @Test
    void emptyData_ShouldWork() {
        byte[] emptyData = new byte[0];
        
        try (SecureMemoryContainer container = new SecureMemoryContainer(emptyData)) {
            assertEquals(0, container.size());
            assertArrayEquals(emptyData, container.getData());
        }
    }

    @Test
    void largeData_ShouldWork() {
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        new java.util.Random().nextBytes(largeData);
        
        try (SecureMemoryContainer container = new SecureMemoryContainer(largeData)) {
            assertEquals(largeData.length, container.size());
            assertArrayEquals(largeData, container.getData());
        }
    }
}
