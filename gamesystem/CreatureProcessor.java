package gamesystem;

import gamesystem.XP.XPChangeAdhoc;
import gamesystem.XP.XPChangeChallenges;
import gamesystem.XP.XPChangeLevel;
import monsters.Monster.MonsterAttackRoutine;
import monsters.StatisticsBlock;
import party.CharacterAttackForm;

// the visitor definition
public interface CreatureProcessor {
	public void processCreature(Creature c);

	public void processProperty(String property, Object value);

	public void processAbilityScore(AbilityScore s);

	public void processAC(AC a);

	public void processAttacks(Attacks a, GrappleModifier g);

	public void processHPs(HPs hps);

	public void processSavingThrow(SavingThrow s);

	public void processSkills(Skills s);

	public void processBuff(Buff b);

	public void processInitiative(InitiativeModifier initiative);

	public void processSanity(Sanity sanity);

	public void processSize(Size size);

	public void processFeat(Feat feat);

	public void processLevel(Levels level, NegativeLevels negLevels);

	public void processXPChange(XPChangeAdhoc xp);

	public void processXPChange(XPChangeChallenges xp);

	public void processXPChange(XPChangeLevel xp);

	public void processCharacterAttackForm(CharacterAttackForm a);

	public void processMonsterAttackForm(MonsterAttackRoutine a);

	public void processMonsterFullAttackForm(MonsterAttackRoutine a);

	public void processHitdice(HitDiceProperty hitDice);

	public void processStatisticsBlock(StatisticsBlock blk);
}
