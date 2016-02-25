package com.bekvon.bukkit.residence.containers;

public enum GuiItems {
    admin(7, 0),
    animalkilling(365, 0),
    animals(383, 90),
    bank(130, 0),
    bed(355, 0),
    brew(379, 0),
    bucket(325, 0),
    bucketempty(325, 0),
    bucketfill(325, 0),
    build(45, 0),
    burn(50, 0),
    button(143, 0),
    cake(354, 0),
    canimals(383, 91),
    chat(386, 0),
    cmonsters(383, 50),
    command(137, 0),
    container(342, 0),
    craft(58, 50),
    creeper(383, 50),
    dragongrief(3, 0),
    day(37, 0),
    damage(283, 0),
    destroy(121, 0),
    diode(356, 0),
    door(324, 0),
    enchant(116, 0),
    explode(407, 0),
    enderpearl(368, 0),
    feed(364, 0),
    fireball(385, 0),
    firespread(377, 0),
    flow(111, 0),
    hidden(102, 0),
    healing(373, 0),
    ignite(259, 0),
    keepinv(298, 0),
    keepexp(322, 0),
    lavaflow(327, 0),
    leash(420, 0),
    lever(69, 0),
    mobexpdrop(362, 0),
    mobitemdrop(351, 3),
    mobkilling(367, 0),
    monsters(52, 0),
    move(301, 0),
    nanimals(383, 92),
    nmonsters(383, 51),
    night(35, 15),
    nofly(171, 1),
    nomobs(166, 0),
    note(25, 0),
    nodurability(145, 0),
    overridepvp(267, 0),
    pressure(147, 0),
    piston(33, 0),
    place(169, 0),
    pvp(268, 0),
    redstone(331, 0),
    respawn(175, 0),
    shop(389, 0),
    snowtrail(78, 0),
    sanimals(383, 101),
    shear(359, 0),
    dye(351, 14),
    smonsters(383, 54),
    subzone(160, 7),
    table(58, 0),
    tnt(46, 0),
    tp(120, 0),
    trusted(170, 0),
    trade(388, 0),
    trample(3, 0),
    use(70, 0),
    vehicledestroy(328, 0),
    witherdamage(49, 0),
    waterflow(326, 0);

    private int id;
    private int data;

    private GuiItems(int id, int data) {
	this.id = id;
	this.data = data;
    }

    public int getId() {
	return id;
    }

    public int getData() {
	return data;
    }
}
