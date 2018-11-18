package gamesystem;

import java.util.ArrayList;
import java.util.List;

import gamesystem.Feat.FeatDefinition;



// Feat is an instance of a FeatDefinition that can be applied to a Creature
public class Feat extends Effector<Feat, FeatDefinition> {
	public boolean bonus = false;	// true if this feat was added due to a class feature. eventually will want to expand to list of sources
	public String target;

	public static List<FeatDefinition> feats = new ArrayList<>();

	Feat(FeatDefinition def) {
		super(def);
	}

	// returns a string that fully identifies this feat: the name with 'B' appended if this is a bonus feat and the target string appended in parentheses, if there is one.
	public String getIdentityString() {
		String s = super.toString();
		if (bonus) s += "B";
		if (definition.hasTarget() && target != null && target.length() > 0) s += " (" + target + ")";
		return s;
	}

	@Override
	public String toString() {
		String s = super.toString();
		if (definition.hasTarget() && target != null && target.length() > 0) s += " (" + target + ")";
		return s;
	}

	// add a suffix of " (X)" to the source of all modifiers in this feat. this is used for repeatable feats to prevent modifiers from being ignored due to having the same source
	public void addOrdinal(int ordinal) {
		for (Modifier m : modifiers) {
			m.setSource(m.getSource() + " (" + ordinal + ")");
		}
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (!(obj instanceof Feat)) return false;
//		Feat f = (Feat) obj;
//		if (f.definition != definition) return false;
//		if (!definition.hasTarget()) return true;
//		if (target != null && f.target != null && target.equals(f.target) || target == null && f.target == null) return true;
//		return false;
//	}

	public static class FeatDefinition extends EffectorDefinition<FeatDefinition> {
		public boolean repeatable = false;
		public String summary;
		public String ref;
		public String target;

		protected FeatDefinition(String name) {
			super(name);
		}

		protected FeatDefinition(String name, boolean repeat) {
			super(name);
			repeatable = repeat;
		}

		public boolean hasTarget() {
			return target != null && target.length() > 0;
		}

		public Feat getFeat() {
			return getFeat(null);
		}

		public Feat getFeat(String target) {
			Feat b = new Feat(this);
			b.target = target;

			for (Effect e : effects) {
				if (e instanceof FixedEffect)
					b.addModifier(((FixedEffect) e).getModifier(name), e.target);
			}
			return b;
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
	public static final String FEAT_WEAPON_FOCUS = "Weapon Focus";
	public static final String FEAT_GREATER_WEAPON_FOCUS = "Greater Weapon Focus";
	public static final String FEAT_WEAPON_SPECIALIZATION = "Weapon Specialization";
	public static final String FEAT_GREATER_WEAPON_SPECIALIZATION = "Greater Weapon Specialization";
}
