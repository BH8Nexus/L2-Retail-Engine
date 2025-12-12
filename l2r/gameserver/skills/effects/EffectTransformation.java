package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.skills.skillclasses.Transformation;
import l2r.gameserver.stats.Env;

public final class EffectTransformation extends Effect
{
	private final boolean isFlyingTransform;
	
	public EffectTransformation(Env env, EffectTemplate template)
	{
		super(env, template);
		int id = (int) template._value;
		isFlyingTransform = template.getParam().getBool("isFlyingTransform", (id == 8) || (id == 9) || (id == 260)); // TODO сделать через параметр
	}
	
	@Override
	public boolean checkCondition()
	{
		if (!_effected.isPlayer())
		{
			return false;
		}
		
		if (_effected.getPlayer().isMounted())
		{
			// Original Message: You cannot transform while mounted.
			_effected.sendMessage(new CustomMessage("l2r.gameserver.skills.effects.EffectTransformation.message1"));
			return false;
		}
		
		if (isFlyingTransform && (_effected.getX() > -166168))
		{
			return false;
		}
		
		// Custom for AQ zone
		if (_effected.isPlayer() && _effected.getPlayer().isInZone(ZoneType.no_transform))
		{
			// Original Message: You are not allowed to transform in this zone.
			_effected.sendMessage(new CustomMessage("l2r.gameserver.skills.effects.EffectTransformation.message2"));
			return false;
		}
		
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if (!_effected.isPlayer())
		{
			return;
		}
		
		Player player = (Player) _effected;
		player.setTransformationTemplate(getSkill().getNpcId());
		if (getSkill() instanceof Transformation)
		{
			player.setTransformationName(((Transformation) getSkill()).transformationName);
		}
		
		int id = (int) calc();
		if (isFlyingTransform)
		{
			boolean isVisible = player.isVisible();
			
			if (player.getPet() != null)
			{
				player.getPet().unSummon();
			}
			
			player.decayMe();
			player.setFlying(true);
			player.setLoc(player.getLoc().changeZ(300)); // Немного поднимаем чара над землей
			
			player.setTransformation(id);
			if (isVisible)
			{
				player.spawnMe();
			}
		}
		else
		{
			player.setTransformation(id);
		}
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		
		if (_effected.isPlayer())
		{
			Player player = (Player) _effected;
			
			if (getSkill() instanceof Transformation)
			{
				player.setTransformationName(null);
			}
			
			if (isFlyingTransform)
			{
				boolean isVisible = player.isVisible();
				player.decayMe();
				player.setFlying(false);
				player.setLoc(player.getLoc().correctGeoZ());
				player.setTransformation(0);
				if (isVisible)
				{
					player.spawnMe();
				}
			}
			else
			{
				player.setTransformation(0);
			}
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}