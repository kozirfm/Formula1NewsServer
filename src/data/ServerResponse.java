package data;

import com.google.gson.annotations.Expose;

public class ServerResponse<T> {
    @Expose
    String state;
    @Expose
    int code;
    @Expose
    String message;
    @Expose
    T result;

    public ServerResponse(String state, int code, String message, T result) {
        this.state = state;
        this.code = code;
        this.message = message;
        this.result = result;
    }
}
