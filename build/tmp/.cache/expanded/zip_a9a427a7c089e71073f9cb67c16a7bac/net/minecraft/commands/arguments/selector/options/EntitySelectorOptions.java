package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class EntitySelectorOptions {
   private static final Map<String, EntitySelectorOptions.Option> OPTIONS = Maps.newHashMap();
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType((p_121520_) -> {
      return Component.translatable("argument.entity.options.unknown", p_121520_);
   });
   public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType((p_121516_) -> {
      return Component.translatable("argument.entity.options.inapplicable", p_121516_);
   });
   public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.distance.negative"));
   public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.level.negative"));
   public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.limit.toosmall"));
   public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType((p_121508_) -> {
      return Component.translatable("argument.entity.options.sort.irreversible", p_121508_);
   });
   public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType((p_121493_) -> {
      return Component.translatable("argument.entity.options.mode.invalid", p_121493_);
   });
   public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType((p_121452_) -> {
      return Component.translatable("argument.entity.options.type.invalid", p_121452_);
   });

   public static void register(String pId, EntitySelectorOptions.Modifier pHandler, Predicate<EntitySelectorParser> pPredicate, Component pTooltip) {
      OPTIONS.put(pId, new EntitySelectorOptions.Option(pHandler, pPredicate, pTooltip));
   }

   public static void bootStrap() {
      if (OPTIONS.isEmpty()) {
         register("name", (p_121425_) -> {
            int i = p_121425_.getReader().getCursor();
            boolean flag = p_121425_.shouldInvertValue();
            String s = p_121425_.getReader().readString();
            if (p_121425_.hasNameNotEquals() && !flag) {
               p_121425_.getReader().setCursor(i);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(p_121425_.getReader(), "name");
            } else {
               if (flag) {
                  p_121425_.setHasNameNotEquals(true);
               } else {
                  p_121425_.setHasNameEquals(true);
               }

               p_121425_.addPredicate((p_175209_) -> {
                  return p_175209_.getName().getString().equals(s) != flag;
               });
            }
         }, (p_121423_) -> {
            return !p_121423_.hasNameEquals();
         }, Component.translatable("argument.entity.options.name.description"));
         register("distance", (p_121421_) -> {
            int i = p_121421_.getReader().getCursor();
            MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromReader(p_121421_.getReader());
            if ((minmaxbounds$doubles.getMin() == null || !(minmaxbounds$doubles.getMin() < 0.0D)) && (minmaxbounds$doubles.getMax() == null || !(minmaxbounds$doubles.getMax() < 0.0D))) {
               p_121421_.setDistance(minmaxbounds$doubles);
               p_121421_.setWorldLimited();
            } else {
               p_121421_.getReader().setCursor(i);
               throw ERROR_RANGE_NEGATIVE.createWithContext(p_121421_.getReader());
            }
         }, (p_121419_) -> {
            return p_121419_.getDistance().isAny();
         }, Component.translatable("argument.entity.options.distance.description"));
         register("level", (p_121417_) -> {
            int i = p_121417_.getReader().getCursor();
            MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromReader(p_121417_.getReader());
            if ((minmaxbounds$ints.getMin() == null || minmaxbounds$ints.getMin() >= 0) && (minmaxbounds$ints.getMax() == null || minmaxbounds$ints.getMax() >= 0)) {
               p_121417_.setLevel(minmaxbounds$ints);
               p_121417_.setIncludesEntities(false);
            } else {
               p_121417_.getReader().setCursor(i);
               throw ERROR_LEVEL_NEGATIVE.createWithContext(p_121417_.getReader());
            }
         }, (p_121415_) -> {
            return p_121415_.getLevel().isAny();
         }, Component.translatable("argument.entity.options.level.description"));
         register("x", (p_121413_) -> {
            p_121413_.setWorldLimited();
            p_121413_.setX(p_121413_.getReader().readDouble());
         }, (p_121411_) -> {
            return p_121411_.getX() == null;
         }, Component.translatable("argument.entity.options.x.description"));
         register("y", (p_121409_) -> {
            p_121409_.setWorldLimited();
            p_121409_.setY(p_121409_.getReader().readDouble());
         }, (p_121407_) -> {
            return p_121407_.getY() == null;
         }, Component.translatable("argument.entity.options.y.description"));
         register("z", (p_121405_) -> {
            p_121405_.setWorldLimited();
            p_121405_.setZ(p_121405_.getReader().readDouble());
         }, (p_121403_) -> {
            return p_121403_.getZ() == null;
         }, Component.translatable("argument.entity.options.z.description"));
         register("dx", (p_121401_) -> {
            p_121401_.setWorldLimited();
            p_121401_.setDeltaX(p_121401_.getReader().readDouble());
         }, (p_121399_) -> {
            return p_121399_.getDeltaX() == null;
         }, Component.translatable("argument.entity.options.dx.description"));
         register("dy", (p_121397_) -> {
            p_121397_.setWorldLimited();
            p_121397_.setDeltaY(p_121397_.getReader().readDouble());
         }, (p_121395_) -> {
            return p_121395_.getDeltaY() == null;
         }, Component.translatable("argument.entity.options.dy.description"));
         register("dz", (p_121562_) -> {
            p_121562_.setWorldLimited();
            p_121562_.setDeltaZ(p_121562_.getReader().readDouble());
         }, (p_121560_) -> {
            return p_121560_.getDeltaZ() == null;
         }, Component.translatable("argument.entity.options.dz.description"));
         register("x_rotation", (p_121558_) -> {
            p_121558_.setRotX(WrappedMinMaxBounds.fromReader(p_121558_.getReader(), true, Mth::wrapDegrees));
         }, (p_121556_) -> {
            return p_121556_.getRotX() == WrappedMinMaxBounds.ANY;
         }, Component.translatable("argument.entity.options.x_rotation.description"));
         register("y_rotation", (p_121554_) -> {
            p_121554_.setRotY(WrappedMinMaxBounds.fromReader(p_121554_.getReader(), true, Mth::wrapDegrees));
         }, (p_121552_) -> {
            return p_121552_.getRotY() == WrappedMinMaxBounds.ANY;
         }, Component.translatable("argument.entity.options.y_rotation.description"));
         register("limit", (p_121550_) -> {
            int i = p_121550_.getReader().getCursor();
            int j = p_121550_.getReader().readInt();
            if (j < 1) {
               p_121550_.getReader().setCursor(i);
               throw ERROR_LIMIT_TOO_SMALL.createWithContext(p_121550_.getReader());
            } else {
               p_121550_.setMaxResults(j);
               p_121550_.setLimited(true);
            }
         }, (p_121548_) -> {
            return !p_121548_.isCurrentEntity() && !p_121548_.isLimited();
         }, Component.translatable("argument.entity.options.limit.description"));
         register("sort", (p_247983_) -> {
            int i = p_247983_.getReader().getCursor();
            String s = p_247983_.getReader().readUnquotedString();
            p_247983_.setSuggestions((p_175153_, p_175154_) -> {
               return SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), p_175153_);
            });
            BiConsumer biconsumer;
            switch (s) {
               case "nearest":
                  biconsumer = EntitySelectorParser.ORDER_NEAREST;
                  break;
               case "furthest":
                  biconsumer = EntitySelectorParser.ORDER_FURTHEST;
                  break;
               case "random":
                  biconsumer = EntitySelectorParser.ORDER_RANDOM;
                  break;
               case "arbitrary":
                  biconsumer = EntitySelector.ORDER_ARBITRARY;
                  break;
               default:
                  p_247983_.getReader().setCursor(i);
                  throw ERROR_SORT_UNKNOWN.createWithContext(p_247983_.getReader(), s);
            }

            p_247983_.setOrder(biconsumer);
            p_247983_.setSorted(true);
         }, (p_121544_) -> {
            return !p_121544_.isCurrentEntity() && !p_121544_.isSorted();
         }, Component.translatable("argument.entity.options.sort.description"));
         register("gamemode", (p_121542_) -> {
            p_121542_.setSuggestions((p_175193_, p_175194_) -> {
               String s1 = p_175193_.getRemaining().toLowerCase(Locale.ROOT);
               boolean flag1 = !p_121542_.hasGamemodeNotEquals();
               boolean flag2 = true;
               if (!s1.isEmpty()) {
                  if (s1.charAt(0) == '!') {
                     flag1 = false;
                     s1 = s1.substring(1);
                  } else {
                     flag2 = false;
                  }
               }

               for(GameType gametype1 : GameType.values()) {
                  if (gametype1.getName().toLowerCase(Locale.ROOT).startsWith(s1)) {
                     if (flag2) {
                        p_175193_.suggest("!" + gametype1.getName());
                     }

                     if (flag1) {
                        p_175193_.suggest(gametype1.getName());
                     }
                  }
               }

               return p_175193_.buildFuture();
            });
            int i = p_121542_.getReader().getCursor();
            boolean flag = p_121542_.shouldInvertValue();
            if (p_121542_.hasGamemodeNotEquals() && !flag) {
               p_121542_.getReader().setCursor(i);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(p_121542_.getReader(), "gamemode");
            } else {
               String s = p_121542_.getReader().readUnquotedString();
               GameType gametype = GameType.byName(s, (GameType)null);
               if (gametype == null) {
                  p_121542_.getReader().setCursor(i);
                  throw ERROR_GAME_MODE_INVALID.createWithContext(p_121542_.getReader(), s);
               } else {
                  p_121542_.setIncludesEntities(false);
                  p_121542_.addPredicate((p_175190_) -> {
                     if (!(p_175190_ instanceof ServerPlayer)) {
                        return false;
                     } else {
                        GameType gametype1 = ((ServerPlayer)p_175190_).gameMode.getGameModeForPlayer();
                        return flag ? gametype1 != gametype : gametype1 == gametype;
                     }
                  });
                  if (flag) {
                     p_121542_.setHasGamemodeNotEquals(true);
                  } else {
                     p_121542_.setHasGamemodeEquals(true);
                  }

               }
            }
         }, (p_121540_) -> {
            return !p_121540_.hasGamemodeEquals();
         }, Component.translatable("argument.entity.options.gamemode.description"));
         register("team", (p_121538_) -> {
            boolean flag = p_121538_.shouldInvertValue();
            String s = p_121538_.getReader().readUnquotedString();
            p_121538_.addPredicate((p_175198_) -> {
               if (!(p_175198_ instanceof LivingEntity)) {
                  return false;
               } else {
                  Team team = p_175198_.getTeam();
                  String s1 = team == null ? "" : team.getName();
                  return s1.equals(s) != flag;
               }
            });
            if (flag) {
               p_121538_.setHasTeamNotEquals(true);
            } else {
               p_121538_.setHasTeamEquals(true);
            }

         }, (p_121536_) -> {
            return !p_121536_.hasTeamEquals();
         }, Component.translatable("argument.entity.options.team.description"));
         register("type", (p_121534_) -> {
            p_121534_.setSuggestions((p_258162_, p_258163_) -> {
               SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), p_258162_, String.valueOf('!'));
               SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTagNames().map(TagKey::location), p_258162_, "!#");
               if (!p_121534_.isTypeLimitedInversely()) {
                  SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), p_258162_);
                  SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTagNames().map(TagKey::location), p_258162_, String.valueOf('#'));
               }

               return p_258162_.buildFuture();
            });
            int i = p_121534_.getReader().getCursor();
            boolean flag = p_121534_.shouldInvertValue();
            if (p_121534_.isTypeLimitedInversely() && !flag) {
               p_121534_.getReader().setCursor(i);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(p_121534_.getReader(), "type");
            } else {
               if (flag) {
                  p_121534_.setTypeLimitedInversely();
               }

               if (p_121534_.isTag()) {
                  TagKey<EntityType<?>> tagkey = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.read(p_121534_.getReader()));
                  p_121534_.addPredicate((p_205691_) -> {
                     return p_205691_.getType().is(tagkey) != flag;
                  });
               } else {
                  ResourceLocation resourcelocation = ResourceLocation.read(p_121534_.getReader());
                  EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.getOptional(resourcelocation).orElseThrow(() -> {
                     p_121534_.getReader().setCursor(i);
                     return ERROR_ENTITY_TYPE_INVALID.createWithContext(p_121534_.getReader(), resourcelocation.toString());
                  });
                  if (Objects.equals(EntityType.PLAYER, entitytype) && !flag) {
                     p_121534_.setIncludesEntities(false);
                  }

                  p_121534_.addPredicate((p_175151_) -> {
                     return Objects.equals(entitytype, p_175151_.getType()) != flag;
                  });
                  if (!flag) {
                     p_121534_.limitToType(entitytype);
                  }
               }

            }
         }, (p_121532_) -> {
            return !p_121532_.isTypeLimited();
         }, Component.translatable("argument.entity.options.type.description"));
         register("tag", (p_121530_) -> {
            boolean flag = p_121530_.shouldInvertValue();
            String s = p_121530_.getReader().readUnquotedString();
            p_121530_.addPredicate((p_175166_) -> {
               if ("".equals(s)) {
                  return p_175166_.getTags().isEmpty() != flag;
               } else {
                  return p_175166_.getTags().contains(s) != flag;
               }
            });
         }, (p_121528_) -> {
            return true;
         }, Component.translatable("argument.entity.options.tag.description"));
         register("nbt", (p_121526_) -> {
            boolean flag = p_121526_.shouldInvertValue();
            CompoundTag compoundtag = (new TagParser(p_121526_.getReader())).readStruct();
            p_121526_.addPredicate((p_175176_) -> {
               CompoundTag compoundtag1 = p_175176_.saveWithoutId(new CompoundTag());
               if (p_175176_ instanceof ServerPlayer) {
                  ItemStack itemstack = ((ServerPlayer)p_175176_).getInventory().getSelected();
                  if (!itemstack.isEmpty()) {
                     compoundtag1.put("SelectedItem", itemstack.save(new CompoundTag()));
                  }
               }

               return NbtUtils.compareNbt(compoundtag, compoundtag1, true) != flag;
            });
         }, (p_121524_) -> {
            return true;
         }, Component.translatable("argument.entity.options.nbt.description"));
         register("scores", (p_121522_) -> {
            StringReader stringreader = p_121522_.getReader();
            Map<String, MinMaxBounds.Ints> map = Maps.newHashMap();
            stringreader.expect('{');
            stringreader.skipWhitespace();

            while(stringreader.canRead() && stringreader.peek() != '}') {
               stringreader.skipWhitespace();
               String s = stringreader.readUnquotedString();
               stringreader.skipWhitespace();
               stringreader.expect('=');
               stringreader.skipWhitespace();
               MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromReader(stringreader);
               map.put(s, minmaxbounds$ints);
               stringreader.skipWhitespace();
               if (stringreader.canRead() && stringreader.peek() == ',') {
                  stringreader.skip();
               }
            }

            stringreader.expect('}');
            if (!map.isEmpty()) {
               p_121522_.addPredicate((p_175201_) -> {
                  Scoreboard scoreboard = p_175201_.getServer().getScoreboard();
                  String s1 = p_175201_.getScoreboardName();

                  for(Map.Entry<String, MinMaxBounds.Ints> entry : map.entrySet()) {
                     Objective objective = scoreboard.getObjective(entry.getKey());
                     if (objective == null) {
                        return false;
                     }

                     if (!scoreboard.hasPlayerScore(s1, objective)) {
                        return false;
                     }

                     Score score = scoreboard.getOrCreatePlayerScore(s1, objective);
                     int i = score.getScore();
                     if (!entry.getValue().matches(i)) {
                        return false;
                     }
                  }

                  return true;
               });
            }

            p_121522_.setHasScores(true);
         }, (p_121518_) -> {
            return !p_121518_.hasScores();
         }, Component.translatable("argument.entity.options.scores.description"));
         register("advancements", (p_121514_) -> {
            StringReader stringreader = p_121514_.getReader();
            Map<ResourceLocation, Predicate<AdvancementProgress>> map = Maps.newHashMap();
            stringreader.expect('{');
            stringreader.skipWhitespace();

            while(stringreader.canRead() && stringreader.peek() != '}') {
               stringreader.skipWhitespace();
               ResourceLocation resourcelocation = ResourceLocation.read(stringreader);
               stringreader.skipWhitespace();
               stringreader.expect('=');
               stringreader.skipWhitespace();
               if (stringreader.canRead() && stringreader.peek() == '{') {
                  Map<String, Predicate<CriterionProgress>> map1 = Maps.newHashMap();
                  stringreader.skipWhitespace();
                  stringreader.expect('{');
                  stringreader.skipWhitespace();

                  while(stringreader.canRead() && stringreader.peek() != '}') {
                     stringreader.skipWhitespace();
                     String s = stringreader.readUnquotedString();
                     stringreader.skipWhitespace();
                     stringreader.expect('=');
                     stringreader.skipWhitespace();
                     boolean flag1 = stringreader.readBoolean();
                     map1.put(s, (p_175186_) -> {
                        return p_175186_.isDone() == flag1;
                     });
                     stringreader.skipWhitespace();
                     if (stringreader.canRead() && stringreader.peek() == ',') {
                        stringreader.skip();
                     }
                  }

                  stringreader.skipWhitespace();
                  stringreader.expect('}');
                  stringreader.skipWhitespace();
                  map.put(resourcelocation, (p_175169_) -> {
                     for(Map.Entry<String, Predicate<CriterionProgress>> entry : map1.entrySet()) {
                        CriterionProgress criterionprogress = p_175169_.getCriterion(entry.getKey());
                        if (criterionprogress == null || !entry.getValue().test(criterionprogress)) {
                           return false;
                        }
                     }

                     return true;
                  });
               } else {
                  boolean flag = stringreader.readBoolean();
                  map.put(resourcelocation, (p_175183_) -> {
                     return p_175183_.isDone() == flag;
                  });
               }

               stringreader.skipWhitespace();
               if (stringreader.canRead() && stringreader.peek() == ',') {
                  stringreader.skip();
               }
            }

            stringreader.expect('}');
            if (!map.isEmpty()) {
               p_121514_.addPredicate((p_175172_) -> {
                  if (!(p_175172_ instanceof ServerPlayer serverplayer)) {
                     return false;
                  } else {
                     PlayerAdvancements playeradvancements = serverplayer.getAdvancements();
                     ServerAdvancementManager serveradvancementmanager = serverplayer.getServer().getAdvancements();

                     for(Map.Entry<ResourceLocation, Predicate<AdvancementProgress>> entry : map.entrySet()) {
                        Advancement advancement = serveradvancementmanager.getAdvancement(entry.getKey());
                        if (advancement == null || !entry.getValue().test(playeradvancements.getOrStartProgress(advancement))) {
                           return false;
                        }
                     }

                     return true;
                  }
               });
               p_121514_.setIncludesEntities(false);
            }

            p_121514_.setHasAdvancements(true);
         }, (p_121506_) -> {
            return !p_121506_.hasAdvancements();
         }, Component.translatable("argument.entity.options.advancements.description"));
         register("predicate", (p_121487_) -> {
            boolean flag = p_121487_.shouldInvertValue();
            ResourceLocation resourcelocation = ResourceLocation.read(p_121487_.getReader());
            p_121487_.addPredicate((p_287325_) -> {
               if (!(p_287325_.level() instanceof ServerLevel)) {
                  return false;
               } else {
                  ServerLevel serverlevel = (ServerLevel)p_287325_.level();
                  LootItemCondition lootitemcondition = serverlevel.getServer().getLootData().getElement(LootDataType.PREDICATE, resourcelocation);
                  if (lootitemcondition == null) {
                     return false;
                  } else {
                     LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.THIS_ENTITY, p_287325_).withParameter(LootContextParams.ORIGIN, p_287325_.position()).create(LootContextParamSets.SELECTOR);
                     LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
                     lootcontext.pushVisitedElement(LootContext.createVisitedEntry(lootitemcondition));
                     return flag ^ lootitemcondition.test(lootcontext);
                  }
               }
            });
         }, (p_121435_) -> {
            return true;
         }, Component.translatable("argument.entity.options.predicate.description"));
      }
   }

   public static EntitySelectorOptions.Modifier get(EntitySelectorParser pParser, String pId, int pCursor) throws CommandSyntaxException {
      EntitySelectorOptions.Option entityselectoroptions$option = OPTIONS.get(pId);
      if (entityselectoroptions$option != null) {
         if (entityselectoroptions$option.canUse.test(pParser)) {
            return entityselectoroptions$option.modifier;
         } else {
            throw ERROR_INAPPLICABLE_OPTION.createWithContext(pParser.getReader(), pId);
         }
      } else {
         pParser.getReader().setCursor(pCursor);
         throw ERROR_UNKNOWN_OPTION.createWithContext(pParser.getReader(), pId);
      }
   }

   public static void suggestNames(EntitySelectorParser pParser, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(Map.Entry<String, EntitySelectorOptions.Option> entry : OPTIONS.entrySet()) {
         if ((entry.getValue()).canUse.test(pParser) && entry.getKey().toLowerCase(Locale.ROOT).startsWith(s)) {
            pBuilder.suggest((String)entry.getKey() + "=", (entry.getValue()).description);
         }
      }

   }

   public interface Modifier {
      void handle(EntitySelectorParser pParser) throws CommandSyntaxException;
   }

   static record Option(EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> canUse, Component description) {
   }
}