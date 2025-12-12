package l2r.gameserver.templates.item.support;

import l2r.gameserver.templates.item.ItemTemplate;

public class EnchantScroll extends EnchantItem
{
	private final FailResultType _resultType;
	private final int _minEncVisEff;
	private final int _maxEncVisEff;
	private final boolean _visualEffect;
	
	public EnchantScroll(int itemId, int chance, int maxEnchant, EnchantType type, ItemTemplate.Grade grade, FailResultType resultType, int minEncVisEff, int maxEncVisEff, boolean visualEffect)
	{
		super(itemId, chance, maxEnchant, type, grade);
		_resultType = resultType;
		_minEncVisEff = minEncVisEff;
		_maxEncVisEff = maxEncVisEff;
		_visualEffect = visualEffect;
	}
	
	public FailResultType getResultType()
	{
		return _resultType;
	}
	
	public boolean showSuccessEffect(int enchant)
	{
		return (enchant >= this._minEncVisEff) && (enchant <= this._maxEncVisEff);
	}
	
	public boolean isHasVisualEffect()
	{
		return _visualEffect;
	}
}
