// priority: 0



console.info('test js mob patch')

/*
IndestructibleEvents.PatchRegistry(event => {
  event.addHumanoidMobPatch("entity_type1", JsHumanoidMobPatchBuilder1)
  event.addHumanoidMobPatch("entity_type2", JsHumanoidMobPatchBuilder2)
  event.addMobPatch("entity_type3", JsMobPatchBuilder3)
  ......
  }
)


const JsHumanoidMobPatchBuilder1 = JsHumanoidMobPatchBuilder.builder().setArmature("armature1")
.setModel("model1")
.setRenderer("renderer1")
.addAttributesByMap(attributemap)
.addLivingAnimationByList(livingMotionMap)
.addHumanoidWeaponMotionByMap(humanoidWeaponMotionMap)
.addHumanoidGuardMotionByMap(humanoidGuardMotionMap)
.addStunAnimationByMap(stunAniamtion)
.addCombatBehaviorByMap(combatBehaviorMap)

const attributemap = AttributeMapHelper.getHelper().addAttribute("attribute1", value1).addAttribute("attribute2", value2).createMap()
const livingMotionMap = LivingMotionHelper.getHelper().addLivingAnimation("livingmotion1", aniamation1).addLivingAnimation("livingmotion2", "animation2").createList()
const humanoidWeaponMotionMap = WeaponMotionHelper.getHelper().addLivingMotions(["categories1, categories2"], "style1", livingMotionMap).createMap();
const guardMotion = GuardMotion.create().setCost(1).setGuardAnimation("animation1").canBlockProjectile(true).setParryAnimations(["animation1", "animation2"]).setParryCost(1)
const humanoidGuardMotionMap = GuardMotionHelper.getHelper().addGuardMotions(["categories1, categories2"], "style1", guardMotion).createMap()
const stunAniamtion = StunAnimationHelper.getHelper().addStunAnimation("stunType1", "animation1").addStunAnimation("stunType2", "animation2").createMap()
const behavior = EFCombatBehaviors.builder().newBehaviorSeries(EFBehaviorSeries.builder().weight(1).nextBehavior(EFBehavior.Builder().animationBehavior("animation2").withinEyeHeight()))
const combatBehaviorMap = CombatBehaviorHelper.getHelper().addCombatBehaviors(["categories1, categories2"], "style1", behavior)
      */

IndestructibleEvents.PatchRegistry(event => 
  event.addHumanoidMobPatch("minecraft:wither_skeleton", 
    JsHumanoidMobPatchBuilder.builder()
    .setModel("epicfight:entity/skeleton")
    .setArmature("epicfight:entity/skeleton")
    .setRenderer("minecraft:skeleton")
    .setFaction("undead")
    .hasBossBar()
    .setBossBarTexture("indestructible:textures/gui/boss_bar.png")
    .setScale(1.5)
    .setChasingSpeed(1)
    .setGuardRadius(3)
    .setAttackRadius(1.5)
    .addAttributesByMap(
      AttributeMapHelper.getHelper()
      .addAttribute("epicfight:impact", 1)
      .addAttribute("epicfight:staminar", 100)
      .createMap()
    )
    .setRegenStaminaStandbyTime(30)
    .hasStunReduction(false)
    .setMaxStunShield(0)
    .setReganShieldMultiply(1)
    .setRegenStaminaStandbyTime(30)
    .setStaminaLoseMultiply(1)
    .initLivingAnimationByDefaultPresent()
    //.addLivingAnimation("idle", "epicfight:biped/living/idle")
    /*
    .addLivingAnimationByList(
      LivingMotionHelper.getHelper()
      .addLivingAnimation("idle", "epicfight:biped/living/idle")
      .addLivingAnimation("walk", "epicfight:biped/living/walk")
      .addLivingAnimation("chase", "epicfight:biped/living/walk")
      .addLivingAnimation("death", "epicfight:biped/living/death")
      .createList()
    )
    */
    .addHumanoidWeaponMotionByMap(
      WeaponMotionHelper.getHelper()
      .addLivingMotions(["sword"], "common", 
        LivingMotionHelper.getHelper()
        .addLivingAnimation("chase", "epicfight:biped/living/hold_longsword")
        .addLivingAnimation("walk", "epicfight:biped/living/walk")
        .createList()
      )
      .createMap()
    )
    /*
    .addHumanoidWeaponMotion(["sword"], "common", 
      LivingMotionHelper.getHelper()
      .addLivingAnimation("chase", "epicfight:biped/living/hold_longsword")
      .addLivingAnimation("walk", "epicfight:biped/living/walk")
      .createList()
    )
      */
    .addHumanoidGuardMotionByMap(
      GuardMotionHelper.getHelper()
      .addGuardMotions(["sword"], "common", 
        GuardMotion.create()
        .canBlockProjectile(true)
        .setCost(3)
        .setParryCost(5)
        .setParryAnimations(["epicfight:biped/skill/guard_longsword_hit_active1", "epicfight:biped/skill/guard_longsword_hit_active2"])
      )
      .createMap()
    )
    /*
    .addHumanoidGuardMotion(["sword"], "common", GuardMotion.create()
    .canBlockProjectile(true)
    .setCost(3)
    .setParryCost(5)
    .setParryAnimations(["epicfight:biped/skill/guard_longsword_hit_active1", "epicfight:biped/skill/guard_longsword_hit_active2"]))
    */
    .addStunAnimationByMap(
      StunAnimationHelper.getHelper()
      .addStunAnimation("short", "epicfight:biped/combat/hit_short")
      .addStunAnimation("long", "epicfight:biped/combat/hit_long")
      .addStunAnimation("knockdown", "epicfight:biped/combat/knockdown")
      .addStunAnimation("fall", "epicfight:biped/living/landing")
      .addStunAnimation("neutralize", "epicfight:biped/skill/guard_break1")
      .createMap()
    )
    //.addStunAnimation("short", "epicfight:biped/combat/hit_short")
    .addStunEvents([
      PatchEvent.createStunEvent("short", e => console.info('test stun event!!!!!!!')), PatchEvent.createStunEvent("short", e => console.info('test stun event again!!!!'))
    ])
    //.addStunEvent(PatchEvent.createStunEvent(e => console.info('test stun event!!!!!!!'), "short"))
    /*
    .addCombatBehaviorByMap(
      CombatBehaviorHelper.getHelper()
      .addCombatBehaviors(["sword"], "common", 
        EFCombatBehaviors.builder().newBehaviorSeries(
          EFBehaviorSeries.builder()
          .canBeInterrupted(false)
          .weight(1)
          .looping(false)
          .nextBehavior(EFBehavior.Builder().tryProcessAnimationSet(AnimationMotionSet.create("epicfight:wither_skeleton/sword_attack1")).process().withinEyeHeight()))
        ).createMap()
    )
    */
    .addCombatBehavior(["sword"], "common", 
      EFCombatBehaviors.builder()
      .newBehaviorSeries(
        EFBehaviorSeries.builder()
        .canBeInterrupted(false).weight(1).looping(false)
        .nextBehavior(EFBehavior.Builder()
        .tryProcessAnimationSet(AnimationMotionSet.create("epicfight:wither_skeleton/sword_attack1")
        .addHitEvent(PatchEvent.createBiEvent((patch, entity)=> {
          console.info('test hit event!!!!!!!')
        })))
        .process().withinEyeHeight())))
  )

)
                                
