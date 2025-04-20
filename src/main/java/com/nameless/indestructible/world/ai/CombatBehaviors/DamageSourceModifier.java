package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.main.Indestructible;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Locale;

public class DamageSourceModifier {
    public float damage;
    public float impact;
    public float armor_negation;
    public StunType stun_type = null;
    public Collider collider = null;

    public DamageSourceModifier(float damage, float impact, float armor_negation) {
        this.damage = damage;
        this.impact = impact;
        this.armor_negation = armor_negation;
    }
    public DamageSourceModifier(){
        this(1F,1F,1F);
    }
    public static DamageSourceModifier create(){
        return new DamageSourceModifier();
    }
    public DamageSourceModifier setDamageModifier(float damage){
        this.damage = damage;
        return this;
    }
    public DamageSourceModifier setImpactModifier(float impact){
        this.impact = impact;
        return this;
    }
    public DamageSourceModifier setArmorNegation(float armor_negation){
        this.armor_negation = armor_negation;
        return this;
    }
    public DamageSourceModifier setStunType(Object object){
        StunType stunType = null;
        if(object instanceof StunType t){
            stunType = t;
        } else if(object instanceof String s){
            stunType = StunType.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.info(object + " can't be recognized");
        this.stun_type = stunType;
        return this;
    }
    public DamageSourceModifier setCollider(Collider collider){
        this.collider = collider;
        return this;
    }
    public DamageSourceModifier setCollider(int number, double sizeX, double sizeY, double sizeZ, double centerX, double centerY, double centerZ){
        if (number == 1) {
            this.collider = new OBBCollider(sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
        } else {
            this.collider = new MultiOBBCollider(number, sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
        }
        return this;
    }
}
