package networking;

public interface OnRequestCallback {

    byte[] handleRequest(byte[] requestPayload);

    String getEndPoint();

}
