package gamesystem;

import java.util.ArrayList;
import java.util.List;

import gamesystem.Feat.FeatDefinition;



// Feat is an instance of a FeatDefinition that can be applied to a Creature
public class Feat extends Feature<Feat, FeatDefinition> {
	public boolean bonus = false;	// true if this feat was added due to a class feature. eventually will want to expand to list of sources

	public static List<FeatDefinition> feats = new ArrayList<>();

	Feat(FeatDefinition def) {
		super(def);
	}

	public static class FeatDefinition extends FeatureDefinition<FeatDefinition> {
		public boolean repeatable = false;
		public String summary;
		public String ref;

		protected FeatDefinition(String name) {
			super(name);
		}

		protected FeatDefinition(String name, boolean repeat) {
			super(name);
			repeatable = repeat;
		}

		public Feat getFeat() {
			Feat b = new Feat(this);

			for (Effect e : effects) {
				if (e instanceof FixedEffect)
					b.modifiers.put(((FixedEffect) e).getModifier(name), e.target);
			}
			return b;
		}

		protected FeatDefinition addSkillBonuses(String skill1, String skill2) {
			addFixedEffect(Creature.STATISTIC_SKILLS + "." + skill1, null, 2, null);
			addFixedEffect(Creature.STATISTIC_SKILLS + "." + skill2, null, 2, null);
			return this;
		}

		FeatDefinition summary(String summary) {
			this.summary = summary;
			return this;
		}

		FeatDefinition ref(String ref) {
			this.ref = ref;
			return this;
		}
	}

	public static FeatDefinition getFeatDefinition(String name) {
		for (FeatDefinition f : feats) {
			if (f.name.equals(name)) return f;
		}
		return null;
	}

	public static final String FEAT_MULTI_WEAPON_FIGHTING = "Multiweapon Fighting";
	public static final String FEAT_MULTIATTACK = "Multiattack";
	public static final String FEAT_POWER_ATTACK = "Power Attack";
	public static final String FEAT_COMBAT_EXPERTISE = "Combat Expertise";
	public static final String FEAT_WEAPON_FINESSE = "Weapon Finesse";
	public static final String FEAT_GREATER_TWO_WEAPON_FIGHTING = "Greater Two-Weapon Fighting";
	public static final String FEAT_IMPROVED_TWO_WEAPON_FIGHTING = "Improved Two-Weapon Fighting";
	public static final String FEAT_TWO_WEAPON_FIGHTING = "Two-Weapon Fighting";
}
