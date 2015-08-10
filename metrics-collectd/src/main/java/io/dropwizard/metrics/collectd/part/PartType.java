package io.dropwizard.metrics.collectd.part;

public enum PartType {

	HOST((short) 0x0000), //
	TIME((short) 0x0001), //
	TIME_HR((short) 0x0008), //
	PLUGIN((short) 0x0002), //
	PLUGIN_INSTANCE((short) 0x0003), //
	TYPE((short) 0x0004), //
	TYPE_INSTANCE((short) 0x0005), //
	VALUES((short) 0x0006), //
	INTERVAL((short) 0x0007), //
	INTERVAL_HR((short) 0x0009), //
	MESSAGE((short) 0x0100), //
	SEVERITY((short) 0x0101), //
	SIGN_SHA256((short) 0x0200), //
	ENCR_AES256((short) 0x0210);

	private final short partTypeCode;

	PartType(final short partTypeCode) {
		this.partTypeCode = partTypeCode;
	}

	public short getPartTypeCode() {
		return partTypeCode;
	}
}
