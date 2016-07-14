package io.dropwizard.metrics.collectd;

enum DataType {

	COUNTER(0), GAUGE(1);

	private final int code;

	private DataType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
