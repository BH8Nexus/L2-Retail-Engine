package l2r.commons.net.nio.impl;

import java.nio.ByteOrder;

public class SelectorConfig
{
	/**
	 * Read buffer size
	 */
	public int READ_BUFFER_SIZE = 65536;
	/**
	 * Buffer size per record
	 */
	public int WRITE_BUFFER_SIZE = 131072;
	/**
	 * The maximum number of packets during a write pass may be less than this number if the write buffer is full.
	 */
	public int MAX_SEND_PER_PASS = 32;
	/**
	 * Delay in milliseconds after each pass in the SelectorThread loop
	 */
	public long SLEEP_TIME = 3;
	/**
	 * Delay before changing a planned action of interest
	 */
	public long INTEREST_DELAY = 3;
	/**
	 * Header size
	 */
	public int HEADER_SIZE = 2;
	/**
	 * Maximum packet size
	 */
	public int PACKET_SIZE = 32768;
	/**
	 * Number of auxiliary buffers
	 */
	public int HELPER_BUFFER_COUNT = 64;
	/**
	 * Client authorization wait timeout in milliseconds
	 */
	public long AUTH_TIMEOUT = 30000L;
	/**
	 * Connection closure timeout in milliseconds
	 */
	public long CLOSEWAIT_TIMEOUT = 10000L;
	/**
	 * Connection Queue Size
	 */
	public int BACKLOG = 1024;
	/**
	 * Byte order
	 */
	public ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
}