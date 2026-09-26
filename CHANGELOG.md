# Changelog

## [0.2.0-beta.1](https://github.com/neojelll/DiaX-Tracker/compare/v0.1.0-beta.1...v0.2.0-beta.1) (2026-09-26)


### Features

* **backup:** export by period and merge on import instead of replacing ([#96](https://github.com/neojelll/DiaX-Tracker/issues/96)) ([cfd6479](https://github.com/neojelll/DiaX-Tracker/commit/cfd6479d2a89e0bbcbb10963eb7901594564bbbc))
* **backup:** make the automatic backup toggle actually back up ([#98](https://github.com/neojelll/DiaX-Tracker/issues/98)) ([360a7f3](https://github.com/neojelll/DiaX-Tracker/commit/360a7f389975ba38400364a42635f3fd48a4819e))
* **history:** drop the edit pencil, tap the card to edit ([#93](https://github.com/neojelll/DiaX-Tracker/issues/93)) ([4c7dd25](https://github.com/neojelll/DiaX-Tracker/commit/4c7dd25a3864995f612d7297235db297c7353fdd))
* **history:** meal tile opens a read-only composition sheet ([#94](https://github.com/neojelll/DiaX-Tracker/issues/94)) ([004013b](https://github.com/neojelll/DiaX-Tracker/commit/004013bf7c77c2840980f2ffa06605449b723496))
* **history:** show sugar after a meal as a pill on the meal card ([#104](https://github.com/neojelll/DiaX-Tracker/issues/104)) ([2effde4](https://github.com/neojelll/DiaX-Tracker/commit/2effde45390b82bf767e68ea00611c375a0588b3))
* **notifications:** yesterday's meal reminder instead of placeholders ([1c90551](https://github.com/neojelll/DiaX-Tracker/commit/1c9055140346cda26fbdffdd0a0658c9f34fe104))
* **toasts:** redesigned toasts with undo for deletions ([75c16f3](https://github.com/neojelll/DiaX-Tracker/commit/75c16f3029ac56263cb3c6261b5fc3044c873bfc))


### Bug Fixes

* **history:** drop the "Сегодня" date filter chip ([#89](https://github.com/neojelll/DiaX-Tracker/issues/89)) ([e1284c6](https://github.com/neojelll/DiaX-Tracker/commit/e1284c64be79b7f151a87748dd7f1f6b57216cfc))
* **history:** match ingredient names in search, not just the meal label ([#88](https://github.com/neojelll/DiaX-Tracker/issues/88)) ([4f5a559](https://github.com/neojelll/DiaX-Tracker/commit/4f5a5593dcbbda213f57f1d94fbdeb53685f25d0))
* **insulin:** slow the active-insulin recompute tick to 1 minute ([#71](https://github.com/neojelll/DiaX-Tracker/issues/71)) ([79e8b17](https://github.com/neojelll/DiaX-Tracker/commit/79e8b17d130e060312f33f7c2b697181484c29bc))
* **nav:** clear system navigation bar under GlukoNavBar and GlukoSheet ([#68](https://github.com/neojelll/DiaX-Tracker/issues/68)) ([48559d2](https://github.com/neojelll/DiaX-Tracker/commit/48559d22e139e431cc56c76dbe74186d1b854b6f))
* **photo:** add in-app camera capture with the new photo picker design ([#92](https://github.com/neojelll/DiaX-Tracker/issues/92)) ([e871bcb](https://github.com/neojelll/DiaX-Tracker/commit/e871bcb0b9c4f9a41f93cf049c9de0870d9de2ed))
* **sensor:** stamp post-meal checks with their exact target time ([#95](https://github.com/neojelll/DiaX-Tracker/issues/95)) ([f08a669](https://github.com/neojelll/DiaX-Tracker/commit/f08a66980880f9e8f82c58fc3e6ef674849a7270))
* **ui:** stop the sugar/insulin/search field cursor clipping against the corner ([#86](https://github.com/neojelll/DiaX-Tracker/issues/86)) ([d1e8d28](https://github.com/neojelll/DiaX-Tracker/commit/d1e8d28fcb1a6a776bd8857b5f578e4ff1e70c98))

## [0.1.0-beta.1](https://github.com/neojelll/DiaX-Tracker/compare/v0.2.0...v0.1.0-beta.1) (2026-09-20)


### Features

* **data:** add bread units (ХЕ) field to diary entries ([#2](https://github.com/neojelll/DiaX-Tracker/issues/2)) ([2d2d74a](https://github.com/neojelll/DiaX-Tracker/commit/2d2d74a9009d6a6a45e113e85c59db8b17ff4260))
* **data:** add Room migrations, sugar source, and entry date picker ([df98bbe](https://github.com/neojelll/DiaX-Tracker/commit/df98bbe416d815ebd4697799f68a7a58504a3c40))
* **data:** allow attaching a photo to a diary entry ([#8](https://github.com/neojelll/DiaX-Tracker/issues/8)) ([d489228](https://github.com/neojelll/DiaX-Tracker/commit/d4892280239c3eca15ceccb6fb97b6c8457f2583))
* **disclaimer:** show a mandatory not-a-medical-device notice on launch ([#60](https://github.com/neojelll/DiaX-Tracker/issues/60)) ([cea4fbf](https://github.com/neojelll/DiaX-Tracker/commit/cea4fbfa542ec2e38ed7fb96228aa4ecf86d5618))
* **entries:** add storage foundation for per-entry product snapshots ([#24](https://github.com/neojelll/DiaX-Tracker/issues/24)) ([37abc50](https://github.com/neojelll/DiaX-Tracker/commit/37abc50b9e72a87e58aa8a207af817e4da92c325))
* **entry-form:** make preset selection editable per entry ([#25](https://github.com/neojelll/DiaX-Tracker/issues/25)) ([45e73d3](https://github.com/neojelll/DiaX-Tracker/commit/45e73d3cde72b44d4e9919dca00febd400320ae7))
* **entry-form:** replace bread units field with a food picker ([#13](https://github.com/neojelll/DiaX-Tracker/issues/13)) ([df29ee0](https://github.com/neojelll/DiaX-Tracker/commit/df29ee09e3222525988f73797a6ef01e6eb46399))
* **entry:** greet by time of day instead of static hello ([#20](https://github.com/neojelll/DiaX-Tracker/issues/20)) ([84e273c](https://github.com/neojelll/DiaX-Tracker/commit/84e273c310ee2237e4c53561ab1c0e413b6eb4ce))
* **entry:** resync sugar from sensor history, not just the latest reading ([#28](https://github.com/neojelll/DiaX-Tracker/issues/28)) ([8e087f3](https://github.com/neojelll/DiaX-Tracker/commit/8e087f3a4bcfca4bac7d4e28336747b29177fb89))
* **food-picker:** apply manual XE live, drop the separate button ([#50](https://github.com/neojelll/DiaX-Tracker/issues/50)) ([0ac0384](https://github.com/neojelll/DiaX-Tracker/commit/0ac03849179df2eb5c9f409d3a19b149e3df87de))
* **history:** add a compact search bar for comments, food, and dates ([#29](https://github.com/neojelll/DiaX-Tracker/issues/29)) ([9b9e3ae](https://github.com/neojelll/DiaX-Tracker/commit/9b9e3ae10ca33dc4eae15490b02488ab40f1569f))
* **history:** add a Вчера (Yesterday) date filter chip ([#53](https://github.com/neojelll/DiaX-Tracker/issues/53)) ([0d0112d](https://github.com/neojelll/DiaX-Tracker/commit/0d0112df66210fe7dd5ba20dfa6ab17427f549ee))
* **history:** add day divider labels to entry list ([#18](https://github.com/neojelll/DiaX-Tracker/issues/18)) ([b57edb8](https://github.com/neojelll/DiaX-Tracker/commit/b57edb8ea086f8dc87ac6fa0b7e9f84242a4617c))
* **history:** show preset-based entries as an expandable box ([#26](https://github.com/neojelll/DiaX-Tracker/issues/26)) ([9836626](https://github.com/neojelll/DiaX-Tracker/commit/98366268c2b37cf8459698c299a81db388692c75))
* **home:** show yesterday's entries at the same time on open ([#38](https://github.com/neojelll/DiaX-Tracker/issues/38)) ([ac644a9](https://github.com/neojelll/DiaX-Tracker/commit/ac644a98c13796cc88d2b120c2c40ae398b2042e))
* **i18n:** add in-app RU/EN language switcher ([#1](https://github.com/neojelll/DiaX-Tracker/issues/1)) ([eedf86b](https://github.com/neojelll/DiaX-Tracker/commit/eedf86b429ed4f455eb1b02310b5cbf592a97760))
* **insulin:** model active insulin as decaying IOB for ultra-short-acting insulin ([#54](https://github.com/neojelll/DiaX-Tracker/issues/54)) ([ac07c64](https://github.com/neojelll/DiaX-Tracker/commit/ac07c644e95e9267bf65eb28d745d6c3c9afc798))
* **meal-presets:** add reusable meal presets with quick insert ([#12](https://github.com/neojelll/DiaX-Tracker/issues/12)) ([1e89fe3](https://github.com/neojelll/DiaX-Tracker/commit/1e89fe3defffeea03fd3175c14ab1338b9a65da0))
* **meal-presets:** support multiple products per preset ([#21](https://github.com/neojelll/DiaX-Tracker/issues/21)) ([cb7a7eb](https://github.com/neojelll/DiaX-Tracker/commit/cb7a7eb9394861022597e4e6edb0f5b830a60079))
* **notifications:** redesign as a bottom sheet, per updated handoff ([#55](https://github.com/neojelll/DiaX-Tracker/issues/55)) ([efe7f46](https://github.com/neojelll/DiaX-Tracker/commit/efe7f466e326fca627cd4b4e7359c20d469676d2))
* **sensor:** accept glucose data from Juggluco and JugglucoNG ([#41](https://github.com/neojelll/DiaX-Tracker/issues/41)) ([f9a2cf9](https://github.com/neojelll/DiaX-Tracker/commit/f9a2cf961e298199f0451515e2058ae1182647ff))
* **sensor:** log every sensor reading instead of only the latest ([#27](https://github.com/neojelll/DiaX-Tracker/issues/27)) ([0794f7a](https://github.com/neojelll/DiaX-Tracker/commit/0794f7a59cee292a7e02269a3fd3fb4f6ea42df9))
* **settings:** add a Settings tab and move language switching there ([#16](https://github.com/neojelll/DiaX-Tracker/issues/16)) ([f51c54b](https://github.com/neojelll/DiaX-Tracker/commit/f51c54b10a12ffa5767dbcf5fa3b7cfde8bb082b))
* **settings:** add manual data export as a zip backup ([#34](https://github.com/neojelll/DiaX-Tracker/issues/34)) ([eb419c6](https://github.com/neojelll/DiaX-Tracker/commit/eb419c67e553959d016dbdda1030051a5cbb0351))
* **settings:** add parameter sheets before export/import file pickers ([#48](https://github.com/neojelll/DiaX-Tracker/issues/48)) ([ab7cc5b](https://github.com/neojelll/DiaX-Tracker/commit/ab7cc5b0442b3076a5e124c976776461cab10251))
* **settings:** add restore-from-backup as the counterpart to export ([#37](https://github.com/neojelll/DiaX-Tracker/issues/37)) ([3eaf074](https://github.com/neojelll/DiaX-Tracker/commit/3eaf074c87df734430628001cf7c62e1d27290cc))
* **settings:** make the blood sugar target range configurable ([#22](https://github.com/neojelll/DiaX-Tracker/issues/22)) ([08e6b59](https://github.com/neojelll/DiaX-Tracker/commit/08e6b5948e4aa8688de395fcd104d1641d2d3c70))
* **theme:** full dark redesign with lime accent and Jones* font ([#35](https://github.com/neojelll/DiaX-Tracker/issues/35)) ([78bcec4](https://github.com/neojelll/DiaX-Tracker/commit/78bcec48af95fe75ac4f0ed08766ddc19d1dd47b))
* **ui:** collapse the top bar fully on scroll ([#11](https://github.com/neojelll/DiaX-Tracker/issues/11)) ([0e9e687](https://github.com/neojelll/DiaX-Tracker/commit/0e9e6877a32a037c3b7880347f03a03fca75ce66))
* **ui:** combine date and time into a single row ([#5](https://github.com/neojelll/DiaX-Tracker/issues/5)) ([9da1835](https://github.com/neojelll/DiaX-Tracker/commit/9da1835a5547775a0a2da8449bd04f9378e0ccbe))
* **ui:** make entry form fields and bottom bar more compact ([#3](https://github.com/neojelll/DiaX-Tracker/issues/3)) ([e7c94fa](https://github.com/neojelll/DiaX-Tracker/commit/e7c94fab4b9930396c130e55861f4d239bb2d52e))
* **ui:** redesign app UI to the Gluko design handoff ([#40](https://github.com/neojelll/DiaX-Tracker/issues/40)) ([177ed2c](https://github.com/neojelll/DiaX-Tracker/commit/177ed2cd5a1a53f08bcf9564f53dbec8abe5822c))
* **ui:** redesign app with a light card-based interface ([#14](https://github.com/neojelll/DiaX-Tracker/issues/14)) ([5137776](https://github.com/neojelll/DiaX-Tracker/commit/513777616c3582a44b0332292218bff5ee817db3))
* **ui:** render single-value history entries as compact rows ([#10](https://github.com/neojelll/DiaX-Tracker/issues/10)) ([3fbcf9f](https://github.com/neojelll/DiaX-Tracker/commit/3fbcf9f5b90bb1976d9c7b13c04c7c34c2aef5f3))
* **ui:** switch app typography to Rubik ([#6](https://github.com/neojelll/DiaX-Tracker/issues/6)) ([dca98a1](https://github.com/neojelll/DiaX-Tracker/commit/dca98a1e44bb98dd72c1b7cfbf99a70f8767327f))
* **ui:** switch background gradient to vertical and rework glass panels ([62c4d24](https://github.com/neojelll/DiaX-Tracker/commit/62c4d242bc707593f34084ee8819792c0de05e1a))


### Bug Fixes

* **copy:** formalize informal/gendered Russian UI text ([#59](https://github.com/neojelll/DiaX-Tracker/issues/59)) ([2183e1f](https://github.com/neojelll/DiaX-Tracker/commit/2183e1fb2e6a77eca6013e06457aed43e3731423))
* **entry-form:** round stepper values to avoid float drift ([#19](https://github.com/neojelll/DiaX-Tracker/issues/19)) ([662b1eb](https://github.com/neojelll/DiaX-Tracker/commit/662b1ebe8bc1d66c3bc1d23c78a8f7955ac4399c))
* **entry:** disable scroll on main screen when content fits ([#45](https://github.com/neojelll/DiaX-Tracker/issues/45)) ([b93a15c](https://github.com/neojelll/DiaX-Tracker/commit/b93a15cfe849e1239c768695e432a354e18e7de1))
* **entry:** don't attach the current sensor reading to backdated entries ([#23](https://github.com/neojelll/DiaX-Tracker/issues/23)) ([c5360f4](https://github.com/neojelll/DiaX-Tracker/commit/c5360f4f1db33851e3bf989f5193ec98b9c4de32))
* **entry:** gate sensor warning on recent activity, move above date ([#56](https://github.com/neojelll/DiaX-Tracker/issues/56)) ([f0b2bb5](https://github.com/neojelll/DiaX-Tracker/commit/f0b2bb5675edaa43a2823bbec2fc045a5e40f2d2))
* **entry:** tidy up comment display and cap its input length ([#58](https://github.com/neojelll/DiaX-Tracker/issues/58)) ([beb33ad](https://github.com/neojelll/DiaX-Tracker/commit/beb33ad9d145cee9f6bb5b30a400d198bf6c633e))
* **error-handling:** stop DB/broadcast exceptions from crashing the app ([#31](https://github.com/neojelll/DiaX-Tracker/issues/31)) ([3d0b253](https://github.com/neojelll/DiaX-Tracker/commit/3d0b253b3663874fde7e11d514ac22bb75e10433))
* **fields:** dismiss keyboard on Done for sugar and insulin fields ([#51](https://github.com/neojelll/DiaX-Tracker/issues/51)) ([341c157](https://github.com/neojelll/DiaX-Tracker/commit/341c157e1246134b61ad608e7689c5ec6c4ea25c))
* **history:** cap the preset/photo row to a strict, equal height ([#57](https://github.com/neojelll/DiaX-Tracker/issues/57)) ([72df8d4](https://github.com/neojelll/DiaX-Tracker/commit/72df8d430893b73f1f19d78c153784a2c4730a22))
* **insulin:** track and surface all overlapping active insulin doses ([#9](https://github.com/neojelll/DiaX-Tracker/issues/9)) ([6937845](https://github.com/neojelll/DiaX-Tracker/commit/6937845e616420e93c193ea5e065a088fb59cb6e))
* **license:** rename font license to match shipped Ruda typeface ([#62](https://github.com/neojelll/DiaX-Tracker/issues/62)) ([77d68f3](https://github.com/neojelll/DiaX-Tracker/commit/77d68f330180ad4f701338588bc86c8bc2f2da3a))
* **presets:** cap preset card size and hint the product limit ([#49](https://github.com/neojelll/DiaX-Tracker/issues/49)) ([23aa7d6](https://github.com/neojelll/DiaX-Tracker/commit/23aa7d63a5b0d50877b1a185be9e92bde4ddf1f8))
* **settings:** lower glucose target range max from 20 to 15 mmol/L ([#52](https://github.com/neojelll/DiaX-Tracker/issues/52)) ([2015182](https://github.com/neojelll/DiaX-Tracker/commit/2015182ca738358ab79da74b3b883ec032d98ee4))
* **settings:** stop double-applying system bar insets ([#46](https://github.com/neojelll/DiaX-Tracker/issues/46)) ([5b5eb66](https://github.com/neojelll/DiaX-Tracker/commit/5b5eb6632c9ef1761ad32cac3173459579768dbb))
* **ui:** center navbar content in pill vertically ([#44](https://github.com/neojelll/DiaX-Tracker/issues/44)) ([0e1ba05](https://github.com/neojelll/DiaX-Tracker/commit/0e1ba053037c65367fab60d54130d6fb0a65402a))
* **ui:** FoodPicker ([#42](https://github.com/neojelll/DiaX-Tracker/issues/42)) ([98686d8](https://github.com/neojelll/DiaX-Tracker/commit/98686d819fabe045ebe818903e1131c77b9e8d66))
* **ui:** lay out history card stats as an even 2-column grid ([#4](https://github.com/neojelll/DiaX-Tracker/issues/4)) ([eac3212](https://github.com/neojelll/DiaX-Tracker/commit/eac3212bd52168e50a1a32276e5f6087794a1fa2))
* **ui:** stop the collapsible header getting stuck mid-collapse ([#17](https://github.com/neojelll/DiaX-Tracker/issues/17)) ([9b2d6a5](https://github.com/neojelll/DiaX-Tracker/commit/9b2d6a5bbd8f604aefeb458771fe5fcaf7b70e16))
* **ui:** stop the insulin banner from overlapping screen content ([#15](https://github.com/neojelll/DiaX-Tracker/issues/15)) ([158d7f1](https://github.com/neojelll/DiaX-Tracker/commit/158d7f1f22ad48426ef259bae5ee3c6436be958b))


### Miscellaneous Chores

* **release:** force the first release to 0.1.0-beta.1 ([#67](https://github.com/neojelll/DiaX-Tracker/issues/67)) ([c40a043](https://github.com/neojelll/DiaX-Tracker/commit/c40a043c63637b45b92eea0922aefcb010ef1080))
