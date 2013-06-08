package com.example.putno;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

public class EngineWaterTemperature extends Command {

	private static byte[] _cmd = new byte[]{0x01, 0x05};
	
	EngineWaterTemperature(OutputStream _os, InputStream _is,
			Lock _lock) {
		super(_cmd, _os, _is, _lock);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int interpret() {
		// TODO Auto-generated method stub
		return ((int)(request[2] & 0xff) - 40);
	}

}
