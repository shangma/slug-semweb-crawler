package com.ldodds.slug.framework;

import java.util.HashMap;
import java.util.Map;

public class ResultImpl implements Result {

	private boolean success;
	private boolean noop;

	private Map<String, Object> context;
	
	public ResultImpl(boolean success, boolean noop) {
		this.success = success;
		this.noop = noop;
		context = new HashMap<String,Object>();
	}

	public void addContext(String url, Object data) {
		context.put(url, data);
	}

	public Object getContext(String url) {
		return context.get(url);
	}

	public Object removeContext(String url) {
		return context.remove(url);
	}
	
	public boolean isNoOp() {
		return noop;
	}

	public boolean isSuccess() {
		return success;
	}
	
	public static Result failure() {
		return new ResultImpl(false, true);
	}
	
	public static Result noop() {
		return new ResultImpl(true, true);
	}
	
}
