package networking;

/**
 * 워커 노드의 WebServer 에서 일어나는
 * 검색 로직을 위한 콜백
 * */
public interface OnRequestCallback {

    byte[] handleRequest(byte[] requestPayload);

    String getEndPoint();

}
