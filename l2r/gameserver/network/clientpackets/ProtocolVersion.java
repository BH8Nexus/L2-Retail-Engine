package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.network.serverpackets.KeyPacket;
import l2r.gameserver.network.serverpackets.SendStatus;
import l2r.gameserver.utils.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolVersion extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ProtocolVersion.class);

	private int _protocol;

	@Override
	protected void readImpl()
	{
		_protocol = readD();
	}

	@Override
	protected void runImpl()
	{
		if (_protocol == -2)
		{
			_client.closeNow(false);
			return;
		}
		else if (_protocol == -3)
		{
			_log.info("Status request from IP : " + getClient().getIpAddr());
			getClient().close(new SendStatus());
			return;
		}
		else if (_protocol < Config.MIN_PROTOCOL_REVISION || _protocol > Config.MAX_PROTOCOL_REVISION)
		{
			_log.warn("Unknown protocol revision : " + _protocol + ", client : " + _client);
			getClient().close(new KeyPacket(null));
			return;
		}

		_client.setRevision(_protocol);

		Log.reachedProtocolVersion(_client, _client.getHWID());
		sendPacket(new KeyPacket(_client.enableCrypt()));
	}

	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}


//package l2r.gameserver.network.clientpackets;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import L2jGuard.L2JConfig;
//import L2jGuard.L2JGuard;
//import l2r.gameserver.Config;
//import l2r.gameserver.network.GameClient;
//import l2r.gameserver.network.serverpackets.KeyPacket;
//import l2r.gameserver.network.serverpackets.SendStatus;
//
///**
// * packet type id 0x0E format: cdbd
// */
//public class ProtocolVersion extends L2GameClientPacket
//{
//	private static final Logger _log = LoggerFactory.getLogger(ProtocolVersion.class);
//	
//	private int _version;
//	private byte[] _data;
//	private String _hwidHdd = "", _hwidMac = "", _hwidCPU = "";
//	
//	@Override
//	protected void readImpl()
//	{
//		GameClient client = getClient();
//		_version = readD();
//		if (_buf.remaining() > 260)
//		{
//			_data = new byte[260];
//			readB(_data);
////			if (L2JGuard.isProtectionOn())
////			{
////				_hwidHdd = readS();
////				_hwidMac = readS();
////				_hwidCPU = readS();
////			}
//		}
////		else if (L2JGuard.isProtectionOn())
////		{
////			client.close(new KeyPacket(null));
////		}
//	}
//	
//	@Override
//	/*
//	 * protected void runImpl() { if(_version == 65534 || _version == -2) { _client.closeNow(false); return; } else if(_version == -3) { _log.info("Status request from IP : " + getClient().getIpAddr()); getClient().close(new SendStatus()); return; } else if(_version < Config.MIN_PROTOCOL_REVISION ||
//	 * _version > Config.MAX_PROTOCOL_REVISION) { _log.warn("Unknown protocol revision : " + _version + ", client : " + _client); getClient().close(new KeyPacket(null)); return; } else if(_version == 65533 || _version == -3) //RWHO { if(Config.RWHO_LOG) { _log.info(getClient().toString() +
//	 * " RWHO received"); } getClient().close(new SendStatus()); } getClient().setRevision(_version); sendPacket(new KeyPacket(_client.enableCrypt())); }
//	 */
//	protected void runImpl()
//	{
//		if (_version == -2)
//		{
//			_client.closeNow(false);
//			return;
//		}
//		else if (_version == -3)
//		{
//			_log.info("Status request from IP : " + getClient().getIpAddr());
//			getClient().close(new SendStatus());
//			return;
//		}
//		else if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION))
//		{
//			_log.warn("Unknown protocol revision : " + _version + ", client : " + _client);
//			getClient().close(new KeyPacket(null));
//			return;
//		}
//		getClient().setRevision(_version);
//		if (L2JGuard.isProtectionOn())
//		{
//			switch (L2JConfig.GET_CLIENT_HWID)
//			{
//				case 1:
//					if (_hwidHdd == "")
//					{
//						_log.info("Status HWID HDD : NoPatch!!!");
//						getClient().close(new KeyPacket(null));
//					}
//					else
//					{
//						getClient().setHWID(_hwidHdd);
//						_log.info("Status HWID HDD : " + getClient().getHWID());
//					}
//					break;
//				case 2:
//					if (_hwidMac == "")
//					{
//						_log.info("Status HWID MAC : NoPatch!!!");
//						getClient().close(new KeyPacket(null));
//					}
//					else
//					{
//						getClient().setHWID(_hwidMac);
//						_log.info("Status HWID MAC : " + getClient().getHWID());
//					}
//					break;
//				case 3:
//					if (_hwidCPU == "")
//					{
//						_log.info("Status HWID : NoPatch!!!");
//						getClient().close(new KeyPacket(null));
//					}
//					else
//					{
//						getClient().setHWID(_hwidCPU);
//						_log.info("Status HWID CPU : " + getClient().getHWID());
//					}
//					break;
//			}
//		}
//		sendPacket(new KeyPacket(_client.enableCrypt()));
//	}
//}