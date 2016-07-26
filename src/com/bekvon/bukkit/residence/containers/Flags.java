package com.bekvon.bukkit.residence.containers;

public enum Flags {
    anvil(145, 0, FlagMode.Both, "Allows or denys interaction with anvil", true),
    admin(7, 0, FlagMode.Player, "Gives a player permission to change flags on a residence", true),
    animalkilling(365, 0, FlagMode.Both, "Allows or denys animal killing", true),
    animals(383, 90, FlagMode.Residence, "Allows or denys animal spawns", true),
    backup(47, 0, FlagMode.Residence, "If set to true, restores previous look of area (WordEdit required)", false),
    bank(130, 0, FlagMode.Both, "Allows or denys deposit/withdraw money from res bank", true),
    bed(355, 0, FlagMode.Both, "Allows or denys players to use beds", true),
    beacon(138, 0, FlagMode.Both, "Allows or denys interaction with beacon", true),
    brew(379, 0, FlagMode.Both, "Allows or denys players to use brewing stands", true),
    bucket(325, 0, FlagMode.Both, "Allow or deny bucket use", true),
    bucketempty(325, 0, FlagMode.Both, "Allow or deny bucket empty", true),
    bucketfill(325, 0, FlagMode.Both, "Allow or deny bucket fill", true),
    build(45, 0, FlagMode.Both, "Allows or denys building", true),
    burn(50, 0, FlagMode.Residence, "Allows or denys Mob combustion in residences", true),
    button(143, 0, FlagMode.Both, "Allows or denys players to use buttons", true),
    cake(354, 0, FlagMode.Both, "Allows or denys players to eat cake", true),
    canimals(383, 91, FlagMode.Residence, "Allows or denys custom animal spawns", true),
    chorustp(432, 0, FlagMode.Both, "Allow or disallow teleporting to the residence with chorus fruit", true),
    chat(386, 0, FlagMode.Both, "Allows to join residence chat room", true),
    cmonsters(383, 50, FlagMode.Residence, "Allows or denys custom monster spawns", true),
    commandblock(137, 0, FlagMode.Both, "Allows or denys command block interaction", false),
    command(137, 0, FlagMode.Both, "Allows or denys comamnd use in residences", false),
    container(342, 0, FlagMode.Both, "Allows or denys use of furnaces, chests, dispensers, etc...", true),
    coords(345, 0, FlagMode.Residence, "Hides residence coordinates", true),
    craft(1, 0, FlagMode.Residence, "Gives table, enchant, brew flags", true),
    creeper(383, 50, FlagMode.Residence, "Allow or deny creeper explosions", true),
    dragongrief(3, 0, FlagMode.Residence, "Prevents ender dragon block griefing", true),
    day(37, 0, FlagMode.Residence, "Sets day time in residence", true),
    dye(351, 14, FlagMode.Both, "Allows or denys sheep dyeing", true),
    damage(283, 0, FlagMode.Residence, "Allows or denys all entity damage within the residence", false),
    destroy(121, 0, FlagMode.Both, "Allows or denys only destruction of blocks, overrides the build flag", true),
    dryup(160, 11, FlagMode.Residence, "Prevents land from drying up", true),
    diode(356, 0, FlagMode.Both, "Allows or denys players to use redstone repeaters", true),
    door(324, 0, FlagMode.Both, "Allows or denys players to use doors and trapdoors", true),
    egg(344, 0, FlagMode.Both, "Allows or denys interaction with dragon egg", true),
    enchant(116, 0, FlagMode.Both, "Allows or denys players to use enchanting tables", true),
    explode(407, 0, FlagMode.Residence, "Allows or denys explosions in residences", true),
    enderpearl(368, 0, FlagMode.Both, "Allow or disallow teleporting to the residence with enderpearl", true),
    feed(364, 0, FlagMode.Residence, "Setting to true makes the residence feed its occupants", true),
    fireball(385, 0, FlagMode.Residence, "Allows or denys fire balls in residences", true),
    firespread(377, 0, FlagMode.Residence, "Allows or denys fire spread", true),
    flow(111, 0, FlagMode.Residence, "Allows or denys liquid flow", true),
    flowerpot(390, 0, FlagMode.Both, "Allows or denys interaction with flower pot", true),
    glow(169, 0, FlagMode.Residence, "Players will start glowing when entering residence", true),
    hotfloor(213, 0, FlagMode.Residence, "Prevent damage from magma blocks", true),
    hidden(102, 0, FlagMode.Residence, "Hides residence from list or listall commands", false),
    hook(346, 0, FlagMode.Both, "Allows or denys fishing rod hooking entities", true),
    healing(373, 0, FlagMode.Residence, "Setting to true makes the residence heal its occupants", true),
    iceform(79, 0, FlagMode.Residence, "Prevents from ice forming", true),
    icemelt(79, 0, FlagMode.Residence, "Prevents ice from melting", true),
    ignite(259, 0, FlagMode.Both, "Allows or denys fire ignition", true),
    keepinv(298, 0, FlagMode.Residence, "Players keeps inventory after death", false),
    keepexp(322, 0, FlagMode.Residence, "Players keeps exp after death", false),
    lavaflow(327, 0, FlagMode.Residence, "Allows or denys lava flow, overrides flow", true),
    leash(420, 0, FlagMode.Both, "Allows or denys aninal leash", true),
    lever(69, 0, FlagMode.Both, "Allows or denys players to use levers", true),
    mobexpdrop(362, 0, FlagMode.Residence, "Prevents mob droping exp on death", true),
    mobitemdrop(351, 3, FlagMode.Residence, "Prevents mob droping items on death", true),
    mobkilling(367, 0, FlagMode.Both, "Allows or denys mob killing", true),
    monsters(52, 0, FlagMode.Residence, "Allows or denys monster spawns", true),
    move(301, 0, FlagMode.Both, "Allows or denys movement in the residence", true),
    nanimals(383, 92, FlagMode.Residence, "Allows or denys natural animal spawns", true),
    nmonsters(383, 51, FlagMode.Residence, "Allows or denys natural monster spawns", true),
    night(35, 15, FlagMode.Residence, "Sets night time in residence", true),
    nofly(171, 1, FlagMode.Both, "Allows or denys fly in residence", false),
    nomobs(166, 0, FlagMode.Residence, "Prevents monsters from entering residence", true),
    note(25, 0, FlagMode.Both, "Allows or denys players to use note blocks", true),
    nodurability(145, 0, FlagMode.Residence, "Prevents item durability loss", false),
    overridepvp(267, 0, FlagMode.Residence, "Overrides any plugin pvp protection", false),
    pressure(147, 0, FlagMode.Both, "Allows or denys players to use pressure plates", true),
    piston(33, 0, FlagMode.Residence, "Allow or deny pistons from pushing or pulling blocks in the residence", true),
    pistonprotection(29, 0, FlagMode.Residence, "Enables or disabled piston block move in or out of residence", true),
    place(169, 0, FlagMode.Both, "Allows or denys only placement of blocks, overrides the build flag", true),
    pvp(268, 0, FlagMode.Residence, "Allow or deny pvp in the residence", true),
    rain(38, 1, FlagMode.Residence, "Sets weather to rainny in residence", true),
    redstone(175, 0, FlagMode.Group, "Gives lever, diode, button, pressure, note flags", true),
    respawn(175, 0, FlagMode.Residence, "Automaticaly respawns player", false),
    riding(329, 0, FlagMode.Both, "Prevent riding a horse", true),
    sun(175, 0, FlagMode.Residence, "Sets weather to sunny in residence", true),
    shop(389, 0, FlagMode.Residence, "Adds residence to special residence shop list", true),
    snowtrail(78, 0, FlagMode.Residence, "Prevents snowman snow trails", true),
    spread(332, 0, FlagMode.Residence, "Prevents block spreading", true),
    snowball(332, 0, FlagMode.Residence, "Prevents snowball knockback", true),
    sanimals(383, 101, FlagMode.Residence, "Allows or denys spawner or spawn egg animal spawns", true),
    shear(359, 0, FlagMode.Both, "Allows or denys sheep shear", true),
    smonsters(383, 54, FlagMode.Residence, "Allows or denys spawner or spawn egg monster spawns", true),
    subzone(160, 7, FlagMode.Both, "Allow a player to make subzones in the residence", true),
    table(58, 0, FlagMode.Both, "Allows or denys players to use workbenches", true),
    tnt(46, 0, FlagMode.Residence, "Allow or deny tnt explosions", true),
    tp(120, 0, FlagMode.Both, "Allow or disallow teleporting to the residence", true),
    trade(388, 0, FlagMode.Both, "Allows or denys villager trading in residence", true),
    trample(3, 0, FlagMode.Residence, "Allows or denys crop trampling in residence", true),
    trusted(1, 0, FlagMode.Group, "Gives build, use, move, container and tp flags", true),
    use(70, 0, FlagMode.Both, "Allows or denys use of doors, lever, buttons, etc...", true),
    vehicledestroy(328, 0, FlagMode.Both, "Allows or denys vehicle destroy", true),
    waterflow(326, 0, FlagMode.Residence, "Allows or denys water flow, overrides flow", true);

    private int id;
    private int data;
    private FlagMode flagMode;
    private String desc;
    private boolean enabled;

    public static enum FlagMode {
	Player, Residence, Both, Group
    }

    private Flags(int id, int data, FlagMode flagMode, String desc, boolean enabled) {
	this.id = id;
	this.data = data;
	this.flagMode = flagMode;
	this.desc = desc;
	this.enabled = enabled;
    }

    public int getId() {
	return id;
    }

    public int getData() {
	return data;
    }

    public String getName() {
	return this.name();
    }

    public FlagMode getFlagMode() {
	return flagMode;
    }

    public String getDesc() {
	return desc;
    }

    public void setDesc(String desc) {
	this.desc = desc;
    }

    public boolean isEnabled() {
	return enabled;
    }

    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }

    public static Flags getFlag(String flag) {
	for (Flags f : Flags.values()) {
	    if (f.getName().equalsIgnoreCase(flag))
		return f;
	}
	return null;
    }
}
