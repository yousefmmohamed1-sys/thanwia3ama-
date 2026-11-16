package com.example.data.curriculum

import com.example.data.model.CurriculumLesson
import com.example.data.model.CurriculumUnit
import com.example.data.model.StudyLanguage
import com.example.data.model.StudyTrack
import com.example.data.model.Subject

object CurriculumData {

    private fun lesson(
        id: String,
        unitId: String,
        subjectId: String,
        ar: String,
        en: String = ar,
        mins: Int = 45
    ) = CurriculumLesson(
        id = id,
        unitId = unitId,
        subjectId = subjectId,
        arabicTitle = ar,
        englishTitle = en,
        estimatedMinutes = mins
    )

    private fun unit(
        id: String,
        subjectId: String,
        ar: String,
        en: String = ar,
        lessons: List<CurriculumLesson>
    ) = CurriculumUnit(
        id = id,
        subjectId = subjectId,
        arabicTitle = ar,
        englishTitle = en,
        lessons = lessons
    )

    private val arabicSubject = Subject(
        id = "arabic",
        arabicName = "اللغة العربية",
        englishName = "Arabic Language",
        emoji = "📖",
        tracks = setOf(StudyTrack.SCI_BIO, StudyTrack.SCI_MATH, StudyTrack.LITERARY),
        units = listOf(
            unit(
                "ar_u1", "arabic", "النحو — الوحدات السبع", "النحو — الوحدات السبع",
                listOf(
                    lesson("ar_l1", "ar_u1", "arabic", "الوحدة الأولى: النطق والإملاء (همزة القطع وألف الوصل)", mins = 45),
                    lesson("ar_l2", "ar_u1", "arabic", "الوحدة الثانية: الأبنية (المشتقات العاملة وغير العاملة)", mins = 60),
                    lesson("ar_l3", "ar_u1", "arabic", "الوحدة الثالثة: النواسخ (كان وأخواتها - كاد - إن وأخواتها)", mins = 60),
                    lesson("ar_l4", "ar_u1", "arabic", "الوحدة الرابعة: إعراب الاسم (المفعولات والحال والتمييز)", mins = 65),
                    lesson("ar_l5", "ar_u1", "arabic", "الوحدة الخامسة: إعراب الفعل (النصب والجزم واقتران جواب الشرط بالفاء)", mins = 60),
                    lesson("ar_l6", "ar_u1", "arabic", "الوحدة السادسة: الأدوات (كم والمنادى وحروف الجر الزائدة ولا النافية)", mins = 50),
                    lesson("ar_l7", "ar_u1", "arabic", "الوحدة السابعة: الممنوع من الصرف", mins = 40)
                )
            ),
            unit(
                "ar_u2", "arabic", "الأدب والبلاغة", "الأدب والبلاغة",
                listOf(
                    lesson("ar_l8", "ar_u2", "arabic", "مدرسة الإحياء والبعث وجيل التطوير", mins = 45),
                    lesson("ar_l9", "ar_u2", "arabic", "الاتجاه الوجداني (خليل مطران) ومدرسة الديوان", mins = 45),
                    lesson("ar_l10", "ar_u2", "arabic", "مدرسة أبولو ومدرسة المهاجر والمدرسة الواقعية", mins = 55),
                    lesson("ar_l11", "ar_u2", "arabic", "أدب النثر: المقال والرواية والقصة القصيرة والمسرحية", mins = 50),
                    lesson("ar_l12", "ar_u2", "arabic", "التجربة الشعرية والوحدة الفنية والخيال الكلي والجزئي", mins = 45)
                )
            ),
            unit(
                "ar_u3", "arabic", "النصوص والقراءة المتحررة وقصة الأيام", "النصوص والقراءة المتحررة وقصة الأيام",
                listOf(
                    lesson("ar_l13", "ar_u3", "arabic", "نصوص شعرية: غربة وحنين، المساء، في رثاء مي، أهواك يا وطني", mins = 60),
                    lesson("ar_l14", "ar_u3", "arabic", "نصوص نثرية: التكافل الاجتماعي في الإسلام، الكنيسة نورت", mins = 45),
                    lesson("ar_l15", "ar_u3", "arabic", "مقالات قراءة: إرادة التغيير، أبو الريحان البيروني، القدس مدينة عربية", mins = 45),
                    lesson("ar_l16", "ar_u3", "arabic", "قصة الأيام (الجزء الأول والثاني) لطه حسين", mins = 60),
                    lesson("ar_l17", "ar_u3", "arabic", "التعبير المقالي والأنماط التعبيرية المتحررة", mins = 40)
                )
            )
        )
    )

    private val englishSubject = Subject(
        id = "english",
        arabicName = "اللغة الإنجليزية",
        englishName = "English",
        emoji = "🇬🇧",
        tracks = setOf(StudyTrack.SCI_BIO, StudyTrack.SCI_MATH, StudyTrack.LITERARY),
        units = listOf(
            unit(
                "en_u1", "english", "الوحدات 1 - 6 (الترم الأول)", "Units 1–6: Vocabulary & Grammar",
                listOf(
                    lesson("en_l1", "en_u1", "english", "Unit 1: Read all about it (Past Simple, Continuous & Present Perfect)", "Unit 1: Read all about it", 50),
                    lesson("en_l2", "en_u1", "english", "Unit 2: Her story (Past Perfect & Comparative Adjectives)", "Unit 2: Her story", 50),
                    lesson("en_l3", "en_u1", "english", "Unit 3: Beyond imagination (Future Forms & Clauses)", "Unit 3: Beyond imagination", 50),
                    lesson("en_l4", "en_u1", "english", "Unit 4: Taking care of ourselves (Modal Verbs & Suggestions)", "Unit 4: Taking care of ourselves", 45),
                    lesson("en_l5", "en_u1", "english", "Unit 5: The future of work (Phrasal Verbs & Verb Patterns)", "Unit 5: The future of work", 50),
                    lesson("en_l6", "en_u1", "english", "Unit 6: Let's get it done! (Causative Have / Get)", "Unit 6: Let's get it done!", 45)
                )
            ),
            unit(
                "en_u2", "english", "الوحدات 7 - 12 (الترم الثاني)", "Units 7–12: Vocabulary & Grammar",
                listOf(
                    lesson("en_l7", "en_u2", "english", "Unit 7: The meaning of success (Adverbs & Present Perfect Continuous)", "Unit 7: The meaning of success", 50),
                    lesson("en_l8", "en_u2", "english", "Unit 8: Work experience (Passive Voice in all Tenses)", "Unit 8: Work experience", 50),
                    lesson("en_l9", "en_u2", "english", "Unit 9: Starting again (Quantifiers & Countable/Uncountable)", "Unit 9: Starting again", 45),
                    lesson("en_l10", "en_u2", "english", "Unit 10: Places of cultural interest (Deduction & Used to)", "Unit 10: Places of cultural interest", 45),
                    lesson("en_l11", "en_u2", "english", "Unit 11: Finding your culture (Relative Clauses & Compound Adjectives)", "Unit 11: Finding your culture", 50),
                    lesson("en_l12", "en_u2", "english", "Unit 12: Myths and fables (Reported Speech)", "Unit 12: Myths and fables", 50)
                )
            ),
            unit(
                "en_u3", "english", "القصة والمهارات (Great Expectations)", "Great Expectations & Writing Skills",
                listOf(
                    lesson("en_l13", "en_u3", "english", "Great Expectations: Chapters 1–6", "Great Expectations: Chapters 1–6", 55),
                    lesson("en_l14", "en_u3", "english", "Great Expectations: Chapters 7–12", "Great Expectations: Chapters 7–12", 55),
                    lesson("en_l15", "en_u3", "english", "Essay Writing, Punctuation & Translation Skills", "Essay Writing & Translation", 45)
                )
            )
        )
    )

    private val secondLanguageSubject = Subject(
        id = "second_lang",
        arabicName = "اللغة الثانية (فرنساوي / ألماني)",
        englishName = "2nd Language (French / German)",
        emoji = "🗣️",
        tracks = setOf(StudyTrack.SCI_BIO, StudyTrack.SCI_MATH, StudyTrack.LITERARY),
        units = listOf(
            unit(
                "sl_fr", "second_lang", "الفرنساوي — Club des sportifs", "French — 3rd Secondary",
                listOf(
                    lesson("sl_fr1", "sl_fr", "second_lang", "Unité 1: Le club des sportifs (الرياضات والضمائر الشخصية)", "Français — Unité 1: Le club des sportifs", 45),
                    lesson("sl_fr2", "sl_fr", "second_lang", "Unité 2: Le club des gourmands (المأكولات وأدوات التجزئة)", "Français — Unité 2: Le club des gourmands", 45),
                    lesson("sl_fr3", "sl_fr", "second_lang", "Unité 3: Le club des explorateurs (الماضي المركب والنفي)", "Français — Unité 3: Le club des explorateurs", 50),
                    lesson("sl_fr4", "sl_fr", "second_lang", "Unité 4: Le club des voyageurs (المواصلات والضمير y)", "Français — Unité 4: Le club des voyageurs", 45)
                )
            ),
            unit(
                "sl_de", "second_lang", "الألماني — Hallo Deutsch", "German — 3rd Secondary",
                listOf(
                    lesson("sl_de1", "sl_de", "second_lang", "Lektion 10: Gesundheit & Körper (الصحة والأفعال الناقصة)", "Deutsch — Lektion 10: Gesundheit", 45),
                    lesson("sl_de2", "sl_de", "second_lang", "Lektion 11: In der Stadt (المواصلات وحروف الجر)", "Deutsch — Lektion 11: In der Stadt", 45),
                    lesson("sl_de3", "sl_de", "second_lang", "Lektion 12: Kundenservice (الأفعال المنفصلة والطلب المهذب)", "Deutsch — Lektion 12: Kundenservice", 45),
                    lesson("sl_de4", "sl_de", "second_lang", "Lektion 13–14: Kleidung & Feste (الملابس والمناسبات والصفات)", "Deutsch — Lektion 13–14: Kleidung & Feste", 50)
                )
            )
        )
    )

    private val physicsSubject = Subject(
        id = "physics",
        arabicName = "الفيزياء",
        englishName = "Physics",
        emoji = "⚡",
        tracks = setOf(StudyTrack.SCI_BIO, StudyTrack.SCI_MATH),
        units = listOf(
            unit(
                "phy_u1", "physics", "الباب الأول: الكهربية التيارية والمغناطيسية", "Unit 1: Current Electricity & Magnetism",
                listOf(
                    lesson("phy_l1", "phy_u1", "physics", "الفصل الأول: التيار الكهربي وقانون أوم وقوانين كيرشوف", "Ch 1: Electric Current, Ohm's Law & Kirchhoff's Laws", 75),
                    lesson("phy_l2", "phy_u1", "physics", "الفصل الثاني: التأثير المغناطيسي للتيار وأجهزة القياس الكهربي", "Ch 2: Magnetic Effect of Current & Measuring Instruments", 75),
                    lesson("phy_l3", "phy_u1", "physics", "الفصل الثالث: الحث الكهرومغناطيسي وقانون فاراداي والدينامو والمحول", "Ch 3: Electromagnetic Induction, Dynamo & Transformer", 80),
                    lesson("phy_l4", "phy_u1", "physics", "الفصل الرابع: دوائر التيار المتردد (RLC) ودائرة الرنين", "Ch 4: Alternating Current Circuits (RLC) & Resonance", 70)
                )
            ),
            unit(
                "phy_u2", "physics", "الباب الثاني: الفيزياء الحديثة", "Unit 2: Modern Physics",
                listOf(
                    lesson("phy_l5", "phy_u2", "physics", "الفصل الخامس: ازدواجية الموجة والجسيم (الظاهرة الكهروضوئية وكومتون)", "Ch 5: Wave-Particle Duality & Photoelectric Effect", 60),
                    lesson("phy_l6", "phy_u2", "physics", "الفصل السادس: الأطياف الذرية ونموذج بور والأشعة السينية", "Ch 6: Atomic Spectra, Bohr Model & X-Rays", 55),
                    lesson("phy_l7", "phy_u2", "physics", "الفصل السابع: الليزر وتطبيقاته", "Ch 7: Laser & Applications", 50),
                    lesson("phy_l8", "phy_u2", "physics", "الفصل الثامن: الإلكترونيات الحديثة (أشباه الموصلات والترانزستور والبوابات)", "Ch 8: Modern Electronics, Transistors & Logic Gates", 65)
                )
            )
        )
    )

    private val chemistrySubject = Subject(
        id = "chemistry",
        arabicName = "الكيمياء",
        englishName = "Chemistry",
        emoji = "🧪",
        tracks = setOf(StudyTrack.SCI_BIO, StudyTrack.SCI_MATH),
        units = listOf(
            unit(
                "chem_u1", "chemistry", "الكيمياء غير العضوية والفيزيائية", "Inorganic & Physical Chemistry",
                listOf(
                    lesson("chem_l1", "chem_u1", "chemistry", "الباب الأول: العناصر الانتقالية والحديد وسبائكه وتفاعلاته", "Ch 1: Transition Elements & Iron Metallurgy", 70),
                    lesson("chem_l2", "chem_u1", "chemistry", "الباب الثاني: التحليل الكيميائي (الكشف عن الأنيونات والكاتيونات والمعايرة)", "Ch 2: Chemical Analysis (Qualitative & Quantitative)", 75),
                    lesson("chem_l3", "chem_u1", "chemistry", "الباب الثالث: الاتزان الكيميائي (قاعدة لوشاتيلييه والاتزان الأيوني وpH)", "Ch 3: Chemical Equilibrium & Ionic Equilibrium", 75),
                    lesson("chem_l4", "chem_u1", "chemistry", "الباب الرابع: الكيمياء الكهربية (الخلايا الجلفانية والتحليلية وقوانين فاراداي)", "Ch 4: Electrochemistry & Faraday's Laws", 75)
                )
            ),
            unit(
                "chem_u2", "chemistry", "الباب الخامس: الكيمياء العضوية", "Ch 5: Organic Chemistry",
                listOf(
                    lesson("chem_l5", "chem_u2", "chemistry", "الألكانات والألكينات والألكاينات وتفاعلات الهيدروكربونات", "Hydrocarbons: Alkanes, Alkenes & Alkynes", 70),
                    lesson("chem_l6", "chem_u2", "chemistry", "الهيدروكربونات الحلقية والبنزين العطري", "Cyclic Hydrocarbons & Aromatic Benzene", 60),
                    lesson("chem_l7", "chem_u2", "chemistry", "مشتقات الهيدروكربونات: الكحولات والفينولات", "Alcohols & Phenols", 65),
                    lesson("chem_l8", "chem_u2", "chemistry", "الأحماض الكربوكسيلية والإسترات وتطبيقاتها", "Carboxylic Acids & Esters", 65)
                )
            )
        )
    )

    private val biologySubject = Subject(
        id = "biology",
        arabicName = "الأحياء",
        englishName = "Biology",
        emoji = "🧬",
        tracks = setOf(StudyTrack.SCI_BIO),
        units = listOf(
            unit(
                "bio_u1", "biology", "الباب الأول: التركيب والوظيفة في الكائنات الحية", "Unit 1: Structure & Function",
                listOf(
                    lesson("bio_l1", "bio_u1", "biology", "الفصل الأول: الدعامة والحركة في النبات والإنسان", "Ch 1: Support & Movement", 65),
                    lesson("bio_l2", "bio_u1", "biology", "الفصل الثاني: التنسيق الهرموني في الكائنات الحية", "Ch 2: Hormonal Coordination", 60),
                    lesson("bio_l3", "bio_u1", "biology", "الفصل الثالث: التكاثر في الكائنات الحية (النبات والإنسان)", "Ch 3: Reproduction in Living Organisms", 80),
                    lesson("bio_l4", "bio_u1", "biology", "الفصل الرابع: المناعة في النبات والإنسان", "Ch 4: Immunity in Plants & Humans", 70)
                )
            ),
            unit(
                "bio_u2", "biology", "الباب الثاني: البيولوجيا الجزيئية", "Unit 2: Molecular Biology",
                listOf(
                    lesson("bio_l5", "bio_u2", "biology", "الفصل الأول: الحمض النووي DNA والمعلومات الوراثية وتضاعف DNA", "Ch 1: DNA & Genetic Information", 70),
                    lesson("bio_l6", "bio_u2", "biology", "الفصل الثاني: الأحماض النووية وتخليق البروتين والهندسة الوراثية", "Ch 2: RNA, Protein Synthesis & Genetic Engineering", 70)
                )
            )
        )
    )

    private val geologySubject = Subject(
        id = "geology",
        arabicName = "الجيولوجيا والعلوم البيئية",
        englishName = "Geology & Environmental Science",
        emoji = "🪨",
        tracks = setOf(StudyTrack.SCI_BIO),
        units = listOf(
            unit(
                "geo_u1", "geology", "أساسيات الجيولوجيا", "Fundamentals of Geology",
                listOf(
                    lesson("geo_l1", "geo_u1", "geology", "الباب الأول: علم الجيولوجيا ومكونات كوكب الأرض والتراكيب الجيولوجية", "Ch 1: Geology, Earth Layers & Geological Structures", 60),
                    lesson("geo_l2", "geo_u1", "geology", "الباب الثاني: المعادن وخصائصها الفيزيائية والبلورية", "Ch 2: Minerals & Physical Properties", 50),
                    lesson("geo_l3", "geo_u1", "geology", "الباب الثالث: الصخور (النارية والرسوبية والمتحولة) ودورة الصخور", "Ch 3: Igneous, Sedimentary & Metamorphic Rocks", 65),
                    lesson("geo_l4", "geo_u1", "geology", "الباب الرابع: الحركات الأرضية والانجراف القاري والألواح التكتونية والزلازل", "Ch 4: Plate Tectonics, Continental Drift & Earthquakes", 65),
                    lesson("geo_l5", "geo_u1", "geology", "الباب الخامس: التوازن في الحركة بين الماء والهواء واليابس (التجوية والترسيب)", "Ch 5: Weathering, Erosion & Deposition", 65)
                )
            ),
            unit(
                "geo_u2", "geology", "العلوم البيئية", "Environmental Sciences",
                listOf(
                    lesson("geo_l6", "geo_u2", "geology", "مفاهيم بيئية والنظام البيئي البحري والصحراوي", "Ecosystems: Marine & Desert", 50),
                    lesson("geo_l7", "geo_u2", "geology", "استنزاف الموارد البيئية ووسائل الحفاظ عليها", "Resource Depletion & Conservation", 45)
                )
            )
        )
    )

    private val pureMathSubject = Subject(
        id = "pure_math",
        arabicName = "الرياضيات البحتة",
        englishName = "Pure Mathematics",
        emoji = "📐",
        tracks = setOf(StudyTrack.SCI_MATH),
        units = listOf(
            unit(
                "pm_u1", "pure_math", "التفاضل والتكامل", "Calculus",
                listOf(
                    lesson("pm_l1", "pm_u1", "pure_math", "اشتقاق الدوال المثلثية والاشتقاق الضمني والبارامتري", "Differentiation of Trig, Implicit & Parametric Functions", 65),
                    lesson("pm_l2", "pm_u1", "pure_math", "تفاضل وتكامل الدوال الأسية واللوغاريتمية", "Exponential & Logarithmic Calculus", 65),
                    lesson("pm_l3", "pm_u1", "pure_math", "سلوك الدالة: القيم العظمى والصغرى والتحدب ونقط الانقلاب", "Curve Sketching, Maxima, Minima & Inflection", 70),
                    lesson("pm_l4", "pm_u1", "pure_math", "طرق التكامل والتكامل المحدد والمساحات والحجوم الدورانية", "Integration Techniques, Areas & Volumes of Revolution", 75)
                )
            ),
            unit(
                "pm_u2", "pure_math", "الجبر والهندسة الفراغية", "Algebra & Solid Geometry",
                listOf(
                    lesson("pm_l5", "pm_u2", "pure_math", "مبدأ العد والتباديل والتوافيق ونظرية ذات الحدين", "Counting Principle, Permutations, Combinations & Binomial Theorem", 65),
                    lesson("pm_l6", "pm_u2", "pure_math", "الأعداد المركبة والصورة المثلثية ونظرية ديموافر", "Complex Numbers & De Moivre's Theorem", 65),
                    lesson("pm_l7", "pm_u2", "pure_math", "المحددات والمصفوفات وحل المعادلات الخطية", "Determinants, Matrices & Linear Systems", 60),
                    lesson("pm_l8", "pm_u2", "pure_math", "الهندسة الفراغية: المتجهات في الفراغ، معادلة الخط المستقيم والمستوى", "3D Vectors, Lines & Planes in Space", 75)
                )
            )
        )
    )

    private val appliedMathSubject = Subject(
        id = "applied_math",
        arabicName = "الرياضيات التطبيقية",
        englishName = "Applied Mathematics",
        emoji = "⚙️",
        tracks = setOf(StudyTrack.SCI_MATH),
        units = listOf(
            unit(
                "am_u1", "applied_math", "الاستاتيكا", "Statics",
                listOf(
                    lesson("am_l1", "am_u1", "applied_math", "الاحتكاك: اتزان جسم على مستوى أفقي ومائل خشن", "Friction on Horizontal & Inclined Planes", 65),
                    lesson("am_l2", "am_u1", "applied_math", "العزوم حول نقطة في البعدين وفي الفراغ الثلاثي", "Moments in 2D & 3D", 65),
                    lesson("am_l3", "am_u1", "applied_math", "القوى المتوازية المستوية واتزان الجسم الجاسئ", "Parallel Forces & Equilibrium of Rigid Bodies", 65),
                    lesson("am_l4", "am_u1", "applied_math", "الازدواجات ومركز الثقل", "Couples & Center of Gravity", 65)
                )
            ),
            unit(
                "am_u2", "applied_math", "الديناميكا", "Dynamics",
                listOf(
                    lesson("am_l5", "am_u2", "applied_math", "تفاضل وتكامل الدوال المتجهة (الإزاحة والسرعة والعجلة)", "Calculus of Vector Functions (Kinematics)", 65),
                    lesson("am_l6", "am_u2", "applied_math", "كمية الحركة وقوانين نيوتن الثلاثة والبكرات البسيطة", "Momentum, Newton's Laws & Pulleys", 75),
                    lesson("am_l7", "am_u2", "applied_math", "الدفع والتصادم", "Impulse & Collisions", 60),
                    lesson("am_l8", "am_u2", "applied_math", "الشغل وطاقة الحركة وطاقة الوضع والقدرة", "Work, Kinetic & Potential Energy, Power", 70)
                )
            )
        )
    )

    private val historySubject = Subject(
        id = "history",
        arabicName = "التاريخ",
        englishName = "History",
        emoji = "📜",
        tracks = setOf(StudyTrack.LITERARY),
        units = listOf(
            unit(
                "his_u1", "history", "تاريخ مصر الحديث", "Modern History of Egypt",
                listOf(
                    lesson("his_l1", "his_u1", "history", "الفصل الأول: الحملة الفرنسية على مصر والشام (1798 - 1801)", mins = 60),
                    lesson("his_l2", "his_u1", "history", "الفصل الثاني: بناء الدولة الحديثة في مصر (محمد علي وخلفاؤه)", mins = 70),
                    lesson("his_l3", "his_u1", "history", "الفصل الثالث: مصر منذ الثورة العرابية حتى الحرب العالمية الأولى", mins = 65),
                    lesson("his_l4", "his_u1", "history", "الفصل الرابع: مصر بعد الحرب العالمية الأولى وثورة 1919 وثورة 23 يوليو", mins = 70)
                )
            ),
            unit(
                "his_u2", "history", "تاريخ العرب الحديث والمعاصر", "Modern Arab History",
                listOf(
                    lesson("his_l5", "his_u2", "history", "الفصل الخامس: التوسع الاستعماري في البلاد العربية قبل الحرب العالمية الأولى", mins = 65),
                    lesson("his_l6", "his_u2", "history", "الفصل السادس: التوسع الاستعماري في البلاد العربية بعد الحرب العالمية الأولى", mins = 65),
                    lesson("his_l7", "his_u2", "history", "الفصل السابع: مصر وقضايا العالم العربي المعاصر (القضية الفلسطينية وحرب أكتوبر 1973)", mins = 70),
                    lesson("his_l8", "his_u2", "history", "الفصل الثامن: ثورتا 25 يناير 2011 و30 يونيو 2013", mins = 45)
                )
            )
        )
    )

    private val geographySubject = Subject(
        id = "geography",
        arabicName = "الجغرافيا السياسية",
        englishName = "Political Geography",
        emoji = "🗺️",
        tracks = setOf(StudyTrack.LITERARY),
        units = listOf(
            unit(
                "gg_u1", "geography", "الوحدة الأولى والثانية: الدولة والحدود", "State & Borders",
                listOf(
                    lesson("gg_l1", "gg_u1", "geography", "الدرس التمهيدي: مدخل لدراسة الجغرافيا السياسية والتقنيات الحديثة", mins = 45),
                    lesson("gg_l2", "gg_u1", "geography", "الدولة: تعريفها وأنواعها وعواصمها والمقومات الطبيعية للدولة", mins = 60),
                    lesson("gg_l3", "gg_u1", "geography", "المقومات البشرية لقوة الدولة والنظام السياسي والانتخابي", mins = 60),
                    lesson("gg_l4", "gg_u1", "geography", "الحدود السياسية: أنواعها ووظائفها وتطور خريطة العالم والمشكلات السياسية", mins = 70)
                )
            ),
            unit(
                "gg_u2", "geography", "الوحدة الثالثة والرابعة: التكتلات والأحلاف", "Blocs & Alliances",
                listOf(
                    lesson("gg_l5", "gg_u2", "geography", "التكتلات الاقتصادية (الاتحاد الأوروبي ومجلس التعاون الخليجي والكوميسا)", mins = 65),
                    lesson("gg_l6", "gg_u2", "geography", "الأحلاف العسكرية (حلف شمال الأطلسي الناتو وحلف وارسو)", mins = 55),
                    lesson("gg_l7", "gg_u2", "geography", "العلاقات الدولية والنظام العالمي الجديد وتأثير العولمة", mins = 55)
                )
            )
        )
    )

    private val philosophySubject = Subject(
        id = "philosophy",
        arabicName = "الفلسفة والمنطق",
        englishName = "Philosophy & Logic",
        emoji = "⚖️",
        tracks = setOf(StudyTrack.LITERARY),
        units = listOf(
            unit(
                "ph_u1", "philosophy", "الفلسفة التطبيقية", "Applied Philosophy",
                listOf(
                    lesson("ph_l1", "ph_u1", "philosophy", "الموضوع الأول: الفلسفة وقضايا البيئة", mins = 60),
                    lesson("ph_l2", "ph_u1", "philosophy", "الموضوع الثاني: رؤية الفلسفة للأخلاق البيولوجية والطبية (البيوتيقا)", mins = 65),
                    lesson("ph_l3", "ph_u1", "philosophy", "الموضوع الثالث: الفلسفة وأخلاقيات المهنة", mins = 60),
                    lesson("ph_l4", "ph_u1", "philosophy", "الموضوع الرابع: التفلسف وعلاقته بالقيم", mins = 55)
                )
            ),
            unit(
                "ph_u2", "philosophy", "المنطق التطبيقي", "Applied Logic",
                listOf(
                    lesson("ph_l5", "ph_u2", "philosophy", "الموضوع الأول: الاستدلال الاستقرائي وتطبيقه في العلوم الطبيعية", mins = 65),
                    lesson("ph_l6", "ph_u2", "philosophy", "الموضوع الثاني: معنى الاستنباط وتطبيقه في العلوم الصورية (المنطق الرمزي)", mins = 65),
                    lesson("ph_l7", "ph_u2", "philosophy", "الموضوع الثالث: التكامل بين المنهج الاستقرائي والمنهج الاستنباطي", mins = 55),
                    lesson("ph_l8", "ph_u2", "philosophy", "الموضوع الرابع: المنطق وتكنولوجيا الاتصال والذكاء الاصطناعي", mins = 50)
                )
            )
        )
    )

    private val psychologySubject = Subject(
        id = "psychology",
        arabicName = "علم النفس والاجتماع",
        englishName = "Psychology & Sociology",
        emoji = "🧠",
        tracks = setOf(StudyTrack.LITERARY),
        units = listOf(
            unit(
                "ps_u1", "psychology", "علم النفس", "Psychology",
                listOf(
                    lesson("ps_l1", "ps_u1", "psychology", "الذكاء الواحد والذكاءات المتعددة (جاردنر)", mins = 50),
                    lesson("ps_l2", "ps_u1", "psychology", "نظريات التعلم وتطبيقاتها التربوية ومبادئ التعلم الجيد", mins = 65),
                    lesson("ps_l3", "ps_u1", "psychology", "النمو الإنساني: مبادئه وعوامله ومراحل الطفولة والمراهقة", mins = 65),
                    lesson("ps_l4", "ps_u1", "psychology", "الشخصية والاتجاهات والقيم وأساليب التوافق والصراعات النفسية", mins = 65)
                )
            ),
            unit(
                "ps_u2", "psychology", "علم الاجتماع", "Sociology",
                listOf(
                    lesson("ps_l5", "ps_u2", "psychology", "النظرية الاجتماعية والعمليات والتفاعل والعلاقات والظاهرة الاجتماعية", mins = 60),
                    lesson("ps_l6", "ps_u2", "psychology", "علاقة علم الاجتماع بالعلوم الأخرى والثقافة والعمل التطوعي والعمل الحر", mins = 60),
                    lesson("ps_l7", "ps_u2", "psychology", "التوظيف الاجتماعي للبحوث: العولمة والتطرف والعنف والتنمية", mins = 60)
                )
            )
        )
    )

    private val economicsStatisticsSubject = Subject(
        id = "econ_stats",
        arabicName = "الاقتصاد والإحصاء",
        englishName = "Economics & Statistics",
        emoji = "📊",
        tracks = setOf(StudyTrack.LITERARY),
        units = listOf(
            unit(
                "ec_u1", "econ_stats", "الاقتصاد", "Economics",
                listOf(
                    lesson("ec_l1", "ec_u1", "econ_stats", "الاقتصاد والمشكلة الاقتصادية وعناصر الإنتاج", mins = 45),
                    lesson("ec_l2", "ec_u1", "econ_stats", "الدخل القومي والنقود والبنوك والمالية العامة والعلاقات الدولية", mins = 60)
                )
            ),
            unit(
                "ec_u2", "econ_stats", "الإحصاء", "Statistics",
                listOf(
                    lesson("ec_l3", "ec_u2", "econ_stats", "الارتباط (بيرسون وسبيرمان) ومعادلة خط الانحدار", "Correlation & Linear Regression", 60),
                    lesson("ec_l4", "ec_u2", "econ_stats", "الاحتمال الشرطي والأحداث المستقلة", "Conditional Probability & Independent Events", 55),
                    lesson("ec_l5", "ec_u2", "econ_stats", "المتغيرات العشوائية المتصلة والمتقطعة والتوزيع الطبيعي", "Random Variables & Normal Distribution", 60)
                )
            )
        )
    )

    val allSubjects: List<Subject> = listOf(
        arabicSubject,
        englishSubject,
        secondLanguageSubject,
        physicsSubject,
        chemistrySubject,
        biologySubject,
        geologySubject,
        pureMathSubject,
        appliedMathSubject,
        historySubject,
        geographySubject,
        philosophySubject,
        psychologySubject,
        economicsStatisticsSubject
    )

    fun getSubjectsFor(track: StudyTrack, language: StudyLanguage): List<Subject> {
        return allSubjects.filter { track in it.tracks }
    }

    fun getAllLessonsFor(track: StudyTrack): List<CurriculumLesson> {
        return allSubjects
            .filter { track in it.tracks }
            .flatMap { it.units }
            .flatMap { it.lessons }
    }

    fun findSubjectById(subjectId: String): Subject? =
        allSubjects.firstOrNull { it.id == subjectId }

    fun findLessonById(lessonId: String): CurriculumLesson? =
        allSubjects.flatMap { it.units }.flatMap { it.lessons }.firstOrNull { it.id == lessonId }
}
