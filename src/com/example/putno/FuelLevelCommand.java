package com.example.putno;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReadWriteLock;

public class FuelLevelCommand extends Command {

	private static byte[] fuelCmd = new byte[]{0x1, 0x2f};
	FuelLevelCommand( OutputStream _os, InputStream _is, ReadWriteLock _lock ) {
		super(fuelCmd, _os, _is, _lock);
		// TODO Auto-generated constructor stub
	}


	@Override
	public int interpret() {
		// TODO Auto-generated method stub
		return (int)((request[2] & 0xff)* 100)/255;
	}


}
