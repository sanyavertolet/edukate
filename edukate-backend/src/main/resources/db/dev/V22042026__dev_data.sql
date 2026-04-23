-- ── Dev seed data — loaded only when spring.flyway.locations includes classpath:db/dev ──────────
-- This file is referenced in application-dev.properties and is NOT loaded in production.
--
-- Dev credentials:
-- ┌────────────┬──────────────┬───────────┐
-- │ username   │ password     │ role      │
-- ├────────────┼──────────────┼───────────┤
-- │ admin      │ admin        │ ADMIN     │
-- │ moderator  │ moderator    │ MODERATOR │
-- │ user       │ user         │ USER      │
-- └────────────┴──────────────┴───────────┘
--
-- MinIO images (upload to bucket edukate-bucket after migrations run).
-- Problem IDs are assigned by insertion order below (BIGSERIAL starts at 1).
-- ┌───────────────┬──────────────────────────────┬───────────────────────────────┐
-- │ problem.code  │ problem image (MinIO key)     │ answer image (MinIO key)      │
-- ├───────────────┼──────────────────────────────┼───────────────────────────────┤
-- │ 2.4.34        │ problems/2/2.4.34.jpg         │ —                             │
-- │ 2.1.30        │ problems/3/2.1.30.jpg         │ —                             │
-- │ 3.2.7         │ problems/4/3.2.7.jpg          │ problems/4/3.2.7.jpg          │
-- │ 4.3.12        │ problems/5/4.3.12.jpg         │ —                             │
-- │ 5.6.19        │ problems/6/5.6.19.jpg         │ —                             │
-- │ 7.4.10        │ problems/8/7.4.10.jpg         │ problems/8/7.4.10.jpg         │
-- │ 10.1.21       │ problems/11/10.1.21.jpg       │ —                             │
-- │ 12.1.25       │ problems/13/12.1.25.jpg       │ problems/13/12.1.25.jpg       │
-- └───────────────┴──────────────────────────────┴───────────────────────────────┘
-- Source files: edukate-problems/savchenko/images/problems/

-- ── Users ────────────────────────────────────────────────────────────────────

INSERT INTO users (name, email, token, roles, status) VALUES
    ('admin',
     'admin@edukate.dev',
     '{bcrypt}$2b$10$54XQyL0GoeCCghj8kNDwn.ZJ2Gs8gq4tykFzza0vupqRj8Cw1HXgC',
     '["ADMIN"]',
     'ACTIVE'),
    ('moderator',
     'moderator@edukate.dev',
     '{bcrypt}$2b$10$LSfggNRRFQttxJ1.DfoBC.tl/YSKeb6jQOhS9k5/laR6EAODNyfHC',
     '["MODERATOR"]',
     'ACTIVE'),
    ('user',
     'user@edukate.dev',
     '{bcrypt}$2b$10$NPh.aEXS8/lbh.4hjnHtze8CDLVOcpmmYKt.pFHz3kwBkrAXuaGku',
     '["USER"]',
     'ACTIVE')
ON CONFLICT (name) DO NOTHING;

-- ── Books ─────────────────────────────────────────────────────────────────────

INSERT INTO books (slug, subject, title, citation, description) VALUES
    ('savchenko',
     'Physics',
     'Problems in General Physics',
     'Savchenko O.Ya., Problems in General Physics, 2nd ed., 2001',
     'Classic Soviet-era physics problem book covering mechanics, thermodynamics, optics and electromagnetism.')
ON CONFLICT (slug) DO NOTHING;

-- ── Problems ─────────────────────────────────────────────────────────────────
-- One or two problems per chapter, picked from the middle of each chapter.
-- Insertion order determines problem IDs (see MinIO table above).

-- Ch.1 Kinematics — 1.3.13 (hard, projectile motion envelope)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '1.3.13', 'savchenko/1.3.13', true,
    '["Движение в поле тяжести. Криволинейное движение"]',
    'Снаряд, вылетев из орудия, попал в точку с координатами $x$ по горизонтали и $y$ по вертикали. Начальная скорость снаряда $v$. Найдите: а) тангенс угла, образуемого стволом орудия с горизонтом; б) границу области возможного попадания снаряда; в) наименьшую начальную скорость снаряда, при которой он может попасть в точку с координатами $x$, $y$. *Указание.* При решении воспользуйтесь тождеством $1/\cos^{2}\varphi = \mathrm{tg}^{2}\varphi + 1$.',
    '[]',
    '[]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.2 Dynamics — 2.4.34 (energy, inelastic collision; has image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '2.4.34', 'savchenko/2.4.34', false,
    '["Энергия системы. Передача энергии. Мощность"]',
    'Два груза массы $m_{1}$ и $m_{2}$ ($m_{1} > m_{2}$) связаны нитью, переброшенной через неподвижный блок. В начальный момент груз массы $m_{1}$ удерживают на высоте $h$ над полом. Затем его без толчка отпускают. Какое количество теплоты выделится при ударе груза о пол? Удар абсолютно неупругий.',
    '[]',
    '["2.4.34.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.2 Dynamics — 2.1.30 (hard, Newton laws; 2 subtasks + image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '2.1.30', 'savchenko/2.1.30', true,
    '["Законы Ньютона"]',
    '',
    '[{"id": "а", "text": "Какую силу надо приложить к телу, чтобы тело соскользнуло с неё? За какое время тело соскользнёт, если к доске приложена сила $F_{0}$, а длина доски равна $l$?"},
      {"id": "б", "text": "С каким ускорением движутся тело и доска, если сила $F_{0}$ действует на тело массы $m_{1}$? (Тело массы $m_{1}$ лежит на доске массы $m_{2}$, находящейся на гладкой горизонтальной плоскости. Коэффициент трения между телом и доской $\\mu$.)"}]',
    '["2.1.30.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.3 Oscillations and Waves — 3.2.7 (pendulum with magnet; 2 subtasks + problem & answer image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '3.2.7', 'savchenko/3.2.7', false,
    '["Период и частота свободных колебаний"]',
    '',
    '[{"id": "а", "text": "Математический маятник — железный шарик массы $m$, висящий на длинной нити, — имеет период $T_{0}$. В присутствии магнита, расположенного чуть ниже шарика, период колебаний стал равным $T$. Определите действующую на шарик магнитную силу."},
      {"id": "б", "text": "Железный шарик маятника поместили между полюсами магнита так, что на него действует горизонтальная магнитная сила. Найдите эту силу и новое положение равновесия шарика, если период его колебаний после включения магнитного поля стал равным $T$."}]',
    '["3.2.7.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.4 Fluid Mechanics — 4.3.12 (hard, triangular notch outflow; has image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '4.3.12', 'savchenko/4.3.12', true,
    '["Движение идеальной жидкости"]',
    'Вода вытекает из широкого сосуда через треугольный вырез в его стенке. Во сколько раз уменьшится скорость понижения уровня воды при изменении высоты её уровня от $H$ до $h$?',
    '[]',
    '["4.3.12.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.5 Molecular Physics — 5.6.19 (adiabatic piston compression; has image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '5.6.19', 'savchenko/5.6.19', false,
    '["Первое начало термодинамики. Теплоёмкость"]',
    'Поршень массы $M$, закрывающий объём $V_{0}$ одноатомного газа при давлении $P_{0}$ и температуре $T_{0}$, движется со скоростью $u$. Определите температуру и объём газа при максимальном сжатии. Система теплоизолирована, теплоёмкостями поршня и сосуда пренебречь.',
    '[]',
    '["5.6.19.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.6 Electrostatics — 6.4.2 (parallel-plate capacitor; 2 subtasks)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '6.4.2', 'savchenko/6.4.2', false,
    '["Конденсаторы"]',
    '',
    '[{"id": "а", "text": "Размеры пластин плоского конденсатора увеличили в два раза. Как изменилась ёмкость конденсатора?"},
      {"id": "б", "text": "Как изменится ёмкость плоского конденсатора, если расстояние между пластинами удвоить? Увеличить в $n$ раз?"}]',
    '[]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.7 Charged Particle Motion — 7.4.10 (hard, charged particles near metallic dihedral; problem & answer image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '7.4.10', 'savchenko/7.4.10', true,
    '["Взаимодействие заряженных частиц"]',
    'Скорости трёх заряженных частиц массы $m$ изображены на рисунке. Расстояние от каждой частицы до ребра металлического двугранного угла $d$. Заряды первых двух частиц, летящих в противоположных направлениях, равны $\pm q$. Найдите скорость третьей нейтральной частицы на бесконечности, если начальная скорость этой частицы равна $v$.',
    '[]',
    '["7.4.10.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.8 Electric Current — 8.3.8 (voltmeter range switching paradox)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '8.3.8', 'savchenko/8.3.8', false,
    '["Электрические цепи"]',
    'Переключая вольтметр на измерение вдвое большего диапазона напряжения (со $100$ на $200$ В), ожидали отклонения стрелки на вдвое меньшее число делений. Однако этого не произошло, хотя в остальной части цепи ничего не изменяли. Большее или меньшее напряжение покажет вольтметр после переключения?',
    '[]',
    '[]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.9 Magnetic Field — 9.2.23 (magnetised plates, magnetic moment estimate)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '9.2.23', 'savchenko/9.2.23', false,
    '["Магнитное поле движущегося заряда. Индукция магнитного поля линейного тока"]',
    'Сила взаимодействия двух тонких намагниченных квадратных пластин, расположенных на расстоянии $H$ друг над другом, равна $F$. Размеры пластин $a \times a \times h$. Оцените магнитный момент единицы объёма пластины, если толщина пластины $h \ll H$, а $H \ll a$.',
    '[]',
    '[]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.10 Charged Particles — 10.1.21 (charged ring in axial magnetic field; has image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '10.1.21', 'savchenko/10.1.21', false,
    '["Движение в однородном магнитном поле"]',
    'Равномерно заряженное кольцо радиуса $R$, линейная плотность заряда которого $\rho$, движется соосно аксиально-симметричному магнитному полю со скоростью $v$. Радиальная составляющая индукции магнитного поля на расстоянии $R$ от оси равна $B_{R}$. Определите момент сил, действующих на кольцо.',
    '[]',
    '["10.1.21.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.11 Electromagnetic Induction — 11.3.19 (hard, transformer short-circuit; 2 subtasks)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '11.3.19', 'savchenko/11.3.19', true,
    '["Взаимная индуктивность. Индуктивность проводников. Трансформаторы"]',
    '',
    '[{"id": "а", "text": "Почему опасно замыкание хотя бы одного витка вторичной обмотки трансформатора?"},
      {"id": "б", "text": "Замыкание витка вторичной обмотки трансформатора приводит иногда к выходу из строя первичной обмотки трансформатора. Почему это происходит?"}]',
    '[]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.12 Electromagnetic Waves — 12.1.25 (reflection law proof; 2 subtasks + image)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '12.1.25', 'savchenko/12.1.25', false,
    '["Свойства, излучение и отражение электромагнитных волн"]',
    'Пользуясь методом, изложенным в задаче 12.1.19, докажите, что угол падения электромагнитной волны равен углу отражения. Рассмотрите случаи:',
    '[{"id": "а", "text": "вектор $E$ электромагнитной волны, падающей на металл, параллелен металлической поверхности;"},
      {"id": "б", "text": "вектор $B$ электромагнитной волны параллелен металлической поверхности."}]',
    '["12.1.25.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.13 Optics — 13.3.10 (Moon photography defocus correction)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '13.3.10', 'savchenko/13.3.10', false,
    '["Оптические системы"]',
    'При фотографировании Луны получено размытое изображение в виде диска радиуса $r_{1}$. Резкое изображение Луны имело бы радиус $r_{2}$. Определите, на какое расстояние нужно сместить фотопластинку, чтобы изображение на ней получилось резким. Фокусное расстояние линзы $f$, диаметр $D$, при этом $r_{2} > D/2 > r_{1}$.',
    '[]',
    '[]'
) ON CONFLICT (key) DO NOTHING;

-- Ch.14 Special Relativity — 14.3.19 (relativistic electron beam density)
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '14.3.19', 'savchenko/14.3.19', false,
    '["Преобразование электрического и магнитного полей"]',
    'Скорость электронов в параллельном пучке $\beta c$. Как изменится плотность электронов при движении относительно пучка со скоростью $\beta_{1}c$ в продольном направлении?',
    '[]',
    '[]'
) ON CONFLICT (key) DO NOTHING;

-- ── Answers ───────────────────────────────────────────────────────────────────

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/1.3.13'),
     'а) $\operatorname{tg}\varphi = \dfrac{v^{2} \pm \sqrt{v^{4} - 2gv^{2}y - g^{2}x^{2}}}{gx}$. б) $y = \dfrac{v^{2}}{2g} - \dfrac{gx^{2}}{2v^{2}}$. в) $v_{\text{мин}} = \sqrt{g\bigl(y + \sqrt{x^{2} + y^{2}}\bigr)}$.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/2.4.34'),
     '$Q = \dfrac{m_{1}gh(m_{1} - m_{2})}{m_{1} + m_{2}}$.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/2.1.30'),
     'а) $F > \mu(m_{2} + m_{1})g$; $t = \sqrt{\dfrac{2lm_{2}}{F_{0} - \mu(m_{2} + m_{1})g}}$. б) $a_{1} = \dfrac{F_{0} - \mu m_{1}g}{m_{1}}$, $a_{2} = \mu g\dfrac{m_{1}}{m_{2}}$.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/3.2.7'),
     'а) $F = mg\left[(T_{0}/T)^{2} - 1\right]$. б) $F = mg\sqrt{(T_{0}/T)^{4} - 1}$; $\cos\varphi = (T/T_{0})^{2}$.',
     '$\varphi$ — угол отклонения нового положения равновесия от вертикали.',
     '["3.2.7.jpg"]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/4.3.12'),
     'Скорость понижения уровня уменьшится в $\left(\dfrac{H}{h}\right)^{5/2}$ раз.',
     'При понижении уровня ширина выреза уменьшается в $H/h$ раз, скорость истечения — в $\sqrt{H/h}$ раз; скорость понижения уровня пропорциональна их произведению: $(H/h)^{2}\cdot\sqrt{H/h}$.',
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/5.6.19'),
     '$T = T_{0}\!\left(1 + \dfrac{Mu^{2}}{3P_{0}V_{0}}\right)$, $V = V_{0}\!\left(\dfrac{3P_{0}V_{0}}{3P_{0}V_{0}+Mu^{2}}\right)^{\!3/2}$.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/6.4.2'),
     'а) Увеличилась в четыре раза. б) Уменьшится в два раза; уменьшится в $n$ раз.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/7.4.10'),
     '$v_{\infty} = \sqrt{v^{2} + \dfrac{q^{2}(\sqrt{2}-1)}{4\pi\varepsilon_{0}md}}$.',
     '', '["7.4.10.jpg"]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/8.3.8'),
     'Большее.',
     'При увеличении диапазона внутреннее сопротивление вольтметра возрастает; ток через цепь уменьшается меньше, чем в два раза, поэтому показание оказывается больше ожидаемого.',
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/9.2.23'),
     '$M = \sqrt{\dfrac{\pi H F}{2\mu_{0} a h^{2}}}$.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/10.1.21'),
     '$N = 2\pi R^{2}\rho v B_{R}$.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

-- Ch.1 Kinematics — 1.1.4 (pion decay, speed of light; has image) — added for submission testing
INSERT INTO problems (book_id, code, key, is_hard, tags, text, subtasks, images)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '1.1.4', 'savchenko/1.1.4', false,
    '["Движение с постоянной скоростью"]',
    'Счетчики $A$ и $B$, регистрирующие момент прихода $\gamma$-кванта, расположены на расстоянии $2$ м друг от друга. В некоторой точке между ними произошел распад $\pi^{0}$-мезона на два $\gamma$-кванта. Найдите положение этой точки, если счетчик $A$ зарегистрировал $\gamma$-квант на $10^{-9}$ с позднее, чем счетчик $B$. Скорость света $3 \cdot 10^{8}$ м/с.',
    '[]',
    '["1.1.4.jpg"]'
) ON CONFLICT (key) DO NOTHING;

-- 11.3.19 — no answer in source data, intentionally omitted.

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/12.1.25'),
     'а) $E'' = -E$, $B'' = B$. б) $E'' = E$.',
     '', '["12.1.25.jpg"]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/13.3.10'),
     'К линзе на расстояние $\Delta l = \dfrac{r_{1} - r_{2}}{D/2 + r_{2}}\,f$.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/14.3.19'),
     'Увеличится в $\dfrac{1 + \beta\beta_{1}}{\sqrt{1 - \beta_{1}^{2}}}$ раз.',
     '', '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answers (problem_id, text, notes, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/1.1.4'),
     'На расстоянии $1{,}15$ м от счётчика $A$.',
     null, '[]')
ON CONFLICT (problem_id) DO NOTHING;

-- ── Submissions (problem 1.1.4, user admin) ───────────────────────────────────
-- Four submissions covering every observable state for UI testing:
--   Sub 1 (id=1, 09:00) → check_result PENDING      → submission stays  PENDING
--   Sub 2 (id=2, 10:00) → check_result MISTAKE       → trigger → FAILED
--   Sub 3 (id=3, 11:00) → check_result SUCCESS        → trigger → SUCCESS
--   Sub 4 (id=4, 12:00) → check_result INTERNAL_ERROR → trigger → FAILED
-- problem_progress after all triggers: latest=FAILED (sub 4), best=SUCCESS (sub 3)
--
-- All IDs are hardcoded so ON CONFLICT (id) DO NOTHING makes inserts idempotent
-- even if individual rows were deleted and the script is re-run.
--
-- Submission image (put this file in MinIO before testing):
--   users/1/submissions/16/1/solution.jpg
--   (userId=1=admin, problemId=16=1.1.4 by insertion order, submissionId=1)

INSERT INTO file_objects (id, key_path, key, type, owner_user_id, metadata, created_at, updated_at) VALUES (
    1,
    'users/1/submissions/16/1/solution.jpg',
    '{"_type": "submission", "userId": 1, "problemId": 16, "submissionId": 1, "fileName": "solution.jpg"}',
    'submission',
    (SELECT id FROM users WHERE name = 'admin'),
    '{"lastModified": "2026-04-23T09:00:00Z", "contentLength": 45678, "contentType": "image/jpeg"}',
    '2026-04-23 09:00:00+00',
    '2026-04-23 09:00:00+00'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO submissions (id, problem_id, user_id, status, file_object_ids, created_at)
VALUES (
    1,
    (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'),
    (SELECT id FROM users WHERE name = 'admin'),
    'PENDING',
    '[1]',
    '2026-04-23 09:00:00+00'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO submissions (id, problem_id, user_id, status, file_object_ids, created_at)
VALUES
    (2, (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'), (SELECT id FROM users WHERE name = 'admin'), 'PENDING', '[]', '2026-04-23 10:00:00+00'),
    (3, (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'), (SELECT id FROM users WHERE name = 'admin'), 'PENDING', '[]', '2026-04-23 11:00:00+00'),
    (4, (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'), (SELECT id FROM users WHERE name = 'admin'), 'PENDING', '[]', '2026-04-23 12:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- Check results — triggers will update submission status and problem_progress automatically.
INSERT INTO check_results (id, submission_id, status, trust_level, error_type, explanation, created_at)
VALUES
    (1, 1, 'PENDING', 0.0, 'NONE', '', '2026-04-23 09:05:00+00'),
    (2, 2, 'MISTAKE', 0.3, 'ALGEBRAIC',
        'The sign of the velocity component was incorrect; the displacement should be measured from counter B, not A.',
        '2026-04-23 10:05:00+00'),
    (3, 3, 'SUCCESS', 0.95, 'NONE',
        'Correct. The time difference Δt = 10⁻⁹ s and c = 3×10⁸ m/s give an offset of 0.15 m from the midpoint, placing the decay point 1.15 m from counter A.',
        '2026-04-23 11:05:00+00'),
    (4, 4, 'INTERNAL_ERROR', 0.0, 'UNCLEAR',
        'Checker could not parse the submitted image; please re-upload a clearer scan.',
        '2026-04-23 12:05:00+00')
ON CONFLICT (id) DO NOTHING;

-- Advance sequences past the hardcoded IDs so future inserts don't collide.
SELECT setval('file_objects_id_seq',  GREATEST((SELECT MAX(id) FROM file_objects),  1));
SELECT setval('submissions_id_seq',   GREATEST((SELECT MAX(id) FROM submissions),   1));
SELECT setval('check_results_id_seq', GREATEST((SELECT MAX(id) FROM check_results), 1));

-- ── Problem sets ──────────────────────────────────────────────────────────────
-- Three problem sets covering every interesting membership scenario:
--
--   Set 1 "Kinematics Practice" — admin owns it; user is a member
--   Set 2 "Classical Mechanics"  — moderator owns it; admin is USER, user is USER
--   Set 3 "Intro to Physics"     — public; moderator owns it; admin + user are members
--
-- UserRole values (problem-set level): ADMIN · USER · MODERATOR

INSERT INTO problem_sets (name, description, is_public, share_code, user_id_role_map, invited_user_ids) VALUES (
    'Kinematics Practice',
    'A focused selection of kinematics problems from Savchenko''s collection. '
    'Covers constant-velocity motion and projectile problems at introductory level. '
    'Created for self-study; private to members only.',
    false,
    'KIN-PRIV-2026',
    jsonb_build_object(
        CAST((SELECT id FROM users WHERE name = 'admin') AS TEXT), 'ADMIN',
        CAST((SELECT id FROM users WHERE name = 'user')  AS TEXT), 'USER'
    ),
    '[]'
);

INSERT INTO problem_sets (name, description, is_public, share_code, user_id_role_map, invited_user_ids) VALUES (
    'Classical Mechanics',
    'Problems spanning dynamics, oscillations, and fluid mechanics, drawn from chapters 2–5. '
    'Suitable as a supplementary problem set for an undergraduate mechanics course. '
    'Managed by the moderation team; members can track their progress per problem.',
    false,
    'MECH-PRIV-2026',
    jsonb_build_object(
        CAST((SELECT id FROM users WHERE name = 'moderator') AS TEXT), 'ADMIN',
        CAST((SELECT id FROM users WHERE name = 'admin')     AS TEXT), 'USER',
        CAST((SELECT id FROM users WHERE name = 'user')      AS TEXT), 'USER'
    ),
    '[]'
);

INSERT INTO problem_sets (name, description, is_public, share_code, user_id_role_map, invited_user_ids) VALUES (
    'Introduction to Physics',
    'A beginner-friendly tour of physics: kinematics, molecular physics, electrostatics, optics, '
    'and a touch of special relativity. Publicly accessible — anyone with the share code can join. '
    'Problems are chosen for clarity and conceptual depth rather than mathematical difficulty.',
    true,
    'INTRO-PUB-2026',
    jsonb_build_object(
        CAST((SELECT id FROM users WHERE name = 'moderator') AS TEXT), 'ADMIN',
        CAST((SELECT id FROM users WHERE name = 'admin')     AS TEXT), 'USER',
        CAST((SELECT id FROM users WHERE name = 'user')      AS TEXT), 'USER'
    ),
    '[]'
);

-- ── Problem set problems ──────────────────────────────────────────────────────

-- Set 1: Kinematics Practice — 1.1.4, 1.3.13
INSERT INTO problem_set_problems (problem_set_id, problem_id, position)
SELECT
    (SELECT id FROM problem_sets WHERE share_code = 'KIN-PRIV-2026'),
    (SELECT id FROM problems WHERE key = p.key),
    p.pos
FROM (VALUES
    ('savchenko/1.1.4',  0),
    ('savchenko/1.3.13', 1)
) AS p(key, pos)
ON CONFLICT (problem_set_id, problem_id) DO NOTHING;

-- Set 2: Classical Mechanics — 2.4.34, 2.1.30, 3.2.7, 4.3.12, 5.6.19
INSERT INTO problem_set_problems (problem_set_id, problem_id, position)
SELECT
    (SELECT id FROM problem_sets WHERE share_code = 'MECH-PRIV-2026'),
    (SELECT id FROM problems WHERE key = p.key),
    p.pos
FROM (VALUES
    ('savchenko/2.4.34', 0),
    ('savchenko/2.1.30', 1),
    ('savchenko/3.2.7',  2),
    ('savchenko/4.3.12', 3),
    ('savchenko/5.6.19', 4)
) AS p(key, pos)
ON CONFLICT (problem_set_id, problem_id) DO NOTHING;

-- Set 3: Introduction to Physics — one problem from each of six different chapters
INSERT INTO problem_set_problems (problem_set_id, problem_id, position)
SELECT
    (SELECT id FROM problem_sets WHERE share_code = 'INTRO-PUB-2026'),
    (SELECT id FROM problems WHERE key = p.key),
    p.pos
FROM (VALUES
    ('savchenko/1.3.13',  0),
    ('savchenko/6.4.2',   1),
    ('savchenko/8.3.8',   2),
    ('savchenko/10.1.21', 3),
    ('savchenko/13.3.10', 4),
    ('savchenko/14.3.19', 5)
) AS p(key, pos)
ON CONFLICT (problem_set_id, problem_id) DO NOTHING;
