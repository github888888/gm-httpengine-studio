package org.gemini.httpengine.library;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/***
 * Http response get from GMHttpEngine request
 * 
 * @author Gemini
 * 
 */
public class GMHttpResponse {

	private byte[] rawData;
    private int httpStatusCode;
	private HttpResponseParser<?> responseParser;
    private Exception exception;
    private Map<String, String> cookies;

	private boolean isFail = false;

    public void setRawData(byte[] data) {
        if (data != null) {
            this.rawData = data;
        } else {
            this.isFail = true;
        }
    }

    public void parseCookies(List<String> cookies) {
        this.cookies = new HashMap<>();
        List<String> sortList = new ArrayList<>();
        for(String data: cookies) {
            String[] properties = data.split("; ");
            sortList.addAll(Arrays.asList(properties));
        }
        for (String data: sortList) {
            String[] properties = data.split("=");
            if (properties.length < 2) {
                continue;
            }
            this.cookies.put(properties[0], properties[1]);
        }
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public byte[] getRawData() {
		this.filterException();
		return this.rawData;
	}

	public HttpResponseParser getResponseParser() {
        return responseParser;
	}

	public void setResponseParser(HttpResponseParser<?> responseParser) {
		this.responseParser = responseParser;
	}

	public String parseAsString() {
        return parseAsString("UTF-8");
	}

	public String parseAsString(String encode) {
		String ret = null;
        this.filterException();
		try {
			ret = new String(this.rawData, encode);
		} catch (UnsupportedEncodingException e) {
			isFail = true;
			throw new GMHttpException("Unsupport encoding " + e.getMessage());
		}
		return ret;
	}

    public void setHttpStatusCode(int code) {
        this.httpStatusCode = code;
        int status = code / 100;
        switch (status) {
        case 4: {
            // 400 code
            this.isFail = true;
            break;
        }
        case 5: {
            // 500 code
            this.isFail = true;
            break;
        }
        }
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
        this.isFail = true;
    }

    public JSONObject parseAsJSON() throws JSONException{
		String result = parseAsString();
		JSONObject obj = new JSONObject(result);
		return obj;
	}

    public boolean isFail() {
        return this.isFail;
    }

	public Object parseData() {
        this.filterException();
		return responseParser.handleResponse(this.rawData);
	}

    private void filterException() throws RuntimeException{
        if (isFail) {
            throw new GMHttpException("Request is failed, with code: " + this.httpStatusCode);
        }
    }
}
