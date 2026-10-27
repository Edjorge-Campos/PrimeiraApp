package com.example.primeiraapp.data;

import com.example.primeiraapp.model.Shift;
import java.util.List;

public interface ShiftCallback {
    void onSuccess(List<Shift> shifts);
    void onError(Exception error);
}
