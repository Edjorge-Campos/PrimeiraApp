package com.example.primeiraapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class ExampleUnitTest {

    @Test
    public void inscricaoRepetida() {
        String userId = "uid-123";
        String shiftId = "turno-1";

        assertFalse(inscrito(userId, shiftId, new java.util.HashSet<String>()));
        java.util.Set<String> subscribed = new java.util.HashSet<>();
        subscribed.add(userId + ":" + shiftId);
        assertTrue(inscrito(userId, shiftId, subscribed));
    }

    @Test
    public void rejeitarInscricaoVazio() {
        assertFalse(validSignup(null, "turno-1"));
        assertFalse(validSignup("uid-123", null));
        assertFalse(validSignup("", "turno-1"));
        assertFalse(validSignup("uid-123", ""));
        assertTrue(validSignup("uid-123", "turno-1"));
    }

    private boolean inscrito(String userId, String shiftId, java.util.Set<String> subscribed) {
        return subscribed.contains(userId + ":" + shiftId);
    }

    private boolean validSignup(String userId, String shiftId) {
        return userId != null && !userId.trim().isEmpty() && shiftId != null && !shiftId.trim().isEmpty();
    }
}