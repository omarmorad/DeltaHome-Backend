-- V4: Backfill Arabic translations for existing seeded content.
-- Fresh databases get Arabic directly from the DataSeeder factories; this
-- migration repairs databases seeded BEFORE the bilingual columns existed.
-- Guarded with "AND <col>_ar IS NULL" so manual translations are never overwritten.

-- ---------- Companies ----------
UPDATE companies SET name_ar = 'شركة الدلتا للتشطيبات',
       description_ar = 'شركة الدلتا للتشطيبات يقدم خدمات موثوقة في جميع أنحاء منطقة الدلتا.'
WHERE name = 'Delta Finishing Co.' AND (name_ar IS NULL OR name_ar = '');

UPDATE companies SET name_ar = 'مجموعة الدلتا للتشطيبات',
       description_ar = 'مجموعة الدلتا للتشطيبات يقدم خدمات موثوقة في جميع أنحاء منطقة الدلتا.'
WHERE name = 'El-Delta Finishing Group' AND (name_ar IS NULL OR name_ar = '');

UPDATE companies SET name_ar = 'دمياط ستار للتشطيبات',
       description_ar = 'دمياط ستار للتشطيبات يقدم خدمات موثوقة في جميع أنحاء منطقة الدلتا.'
WHERE name = 'Damietta Star Finishing' AND (name_ar IS NULL OR name_ar = '');

UPDATE companies SET name_ar = 'النيل للصيانة والخدمات',
       description_ar = 'النيل للصيانة والخدمات يقدم خدمات موثوقة في جميع أنحاء منطقة الدلتا.'
WHERE name = 'Nile Maintenance & Services' AND (name_ar IS NULL OR name_ar = '');

UPDATE companies SET name_ar = 'دلتا برايم للتطوير العقاري',
       description_ar = 'دلتا برايم للتطوير العقاري يقدم خدمات موثوقة في جميع أنحاء منطقة الدلتا.'
WHERE name = 'Delta Prime Real Estate' AND (name_ar IS NULL OR name_ar = '');

-- ---------- Properties ----------
UPDATE properties SET title_ar = 'شقة بإطلالة بحرية - رأس البر',
       description_ar = 'شقة بإطلالة بحرية - رأس البر في رأس البر. وحدة بموقع مميز وتشطيب سوبر لوكس وجاهزة للسكن الفوري. قريبة من الخدمات والمدارس ومواصلات النقل العام.'
WHERE title = 'Sea View Apartment - Ras El Bar' AND (title_ar IS NULL OR title_ar = '');

UPDATE properties SET title_ar = 'دوبلكس مودرن في دمياط الجديدة',
       description_ar = 'دوبلكس مودرن في دمياط الجديدة في دمياط الجديدة. وحدة بموقع مميز وتشطيب سوبر لوكس وجاهزة للسكن الفوري. قريبة من الخدمات والمدارس ومواصلات النقل العام.'
WHERE title = 'Modern Duplex in New Damietta' AND (title_ar IS NULL OR title_ar = '');

UPDATE properties SET title_ar = 'فيلا عائلية في دمياط',
       description_ar = 'فيلا عائلية في دمياط في دمياط. وحدة بموقع مميز وتشطيب سوبر لوكس وجاهزة للسكن الفوري. قريبة من الخدمات والمدارس ومواصلات النقل العام.'
WHERE title = 'Family Villa in Damietta' AND (title_ar IS NULL OR title_ar = '');

UPDATE properties SET title_ar = 'استوديو في توري - المنصورة',
       description_ar = 'استوديو في توري - المنصورة في المنصورة. وحدة بموقع مميز وتشطيب سوبر لوكس وجاهزة للسكن الفوري. قريبة من الخدمات والمدارس ومواصلات النقل العام.'
WHERE title = 'Studio in Toriel - Mansoura' AND (title_ar IS NULL OR title_ar = '');

UPDATE properties SET title_ar = 'الدور الأرضي مع حديقة - توري',
       description_ar = 'الدور الأرضي مع حديقة - توري في المنصورة. وحدة بموقع مميز وتشطيب سوبر لوكس وجاهزة للسكن الفوري. قريبة من الخدمات والمدارس ومواصلات النقل العام.'
WHERE title = 'Ground Floor with Garden - Toriel' AND (title_ar IS NULL OR title_ar = '');

UPDATE properties SET title_ar = 'شقة مفروشة - كورنيش دمياط الجديدة',
       description_ar = 'شقة مفروشة - كورنيش دمياط الجديدة في دمياط الجديدة. وحدة بموقع مميز وتشطيب سوبر لوكس وجاهزة للسكن الفوري. قريبة من الخدمات والمدارس ومواصلات النقل العام.'
WHERE title = 'Furnished Apartment - New Damietta Corniche' AND (title_ar IS NULL OR title_ar = '');

UPDATE properties SET title_ar = 'فيلا شاطئ - رأس البر',
       description_ar = 'فيلا شاطئ - رأس البر في رأس البر. وحدة بموقع مميز وتشطيب سوبر لوكس وجاهزة للسكن الفوري. قريبة من الخدمات والمدارس ومواصلات النقل العام.'
WHERE title = 'Beach Villa - Ras El Bar' AND (title_ar IS NULL OR title_ar = '');
