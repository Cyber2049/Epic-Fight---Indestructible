// priority: 0



console.info('test js mob patch')

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
                                
