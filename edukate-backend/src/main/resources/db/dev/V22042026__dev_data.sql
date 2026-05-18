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
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '1.3.13', 'savchenko/1.3.13', true,
    '[]',
    '2025-06-01T10:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/1.3.13'),
    'RU',
    'Снаряд, вылетев из орудия, попал в точку с координатами $x$ по горизонтали и $y$ по вертикали. Начальная скорость снаряда $v$. Найдите: а) тангенс угла, образуемого стволом орудия с горизонтом; б) границу области возможного попадания снаряда; в) наименьшую начальную скорость снаряда, при которой он может попасть в точку с координатами $x$, $y$. *Указание.* При решении воспользуйтесь тождеством $1/\cos^{2}\varphi = \mathrm{tg}^{2}\varphi + 1$.',
    '["Движение в поле тяжести. Криволинейное движение"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/1.3.13'),
    'EN',
    'A shell fired from a cannon lands at a point with horizontal coordinate $x$ and vertical coordinate $y$. The initial speed of the shell is $v$. Find: a) the tangent of the angle between the barrel and the horizontal; b) the boundary of the region of possible impact; c) the minimum initial speed of the shell required to reach the point $(x, y)$. *Hint.* Use the identity $1/\cos^{2}\varphi = \tan^{2}\varphi + 1$.',
    '["Motion in gravitational field. Curvilinear motion"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.2 Dynamics — 2.4.34 (energy, inelastic collision; has image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '2.4.34', 'savchenko/2.4.34', false,
    '["2.4.34.jpg"]',
    '2025-07-15T14:30:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/2.4.34'),
    'RU',
    'Два груза массы $m_{1}$ и $m_{2}$ ($m_{1} > m_{2}$) связаны нитью, переброшенной через неподвижный блок. В начальный момент груз массы $m_{1}$ удерживают на высоте $h$ над полом. Затем его без толчка отпускают. Какое количество теплоты выделится при ударе груза о пол? Удар абсолютно неупругий.',
    '["Энергия системы. Передача энергии. Мощность"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/2.4.34'),
    'EN',
    'Two masses $m_{1}$ and $m_{2}$ ($m_{1} > m_{2}$) are connected by a string passed over a fixed pulley. Initially, mass $m_{1}$ is held at height $h$ above the floor and then released without a push. How much heat is released when the mass hits the floor? The collision is perfectly inelastic.',
    '["System energy. Energy transfer. Power"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.2 Dynamics — 2.1.30 (hard, Newton laws; 2 subtasks + image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '2.1.30', 'savchenko/2.1.30', true,
    '["2.1.30.jpg"]',
    '2025-07-15T14:45:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/2.1.30'),
    'RU',
    '',
    '["Законы Ньютона"]',
    '[{"code": "а", "text": "Какую силу надо приложить к телу, чтобы тело соскользнуло с неё? За какое время тело соскользнёт, если к доске приложена сила $F_{0}$, а длина доски равна $l$?"},
      {"code": "б", "text": "С каким ускорением движутся тело и доска, если сила $F_{0}$ действует на тело массы $m_{1}$? (Тело массы $m_{1}$ лежит на доске массы $m_{2}$, находящейся на гладкой горизонтальной плоскости. Коэффициент трения между телом и доской $\\mu$.)"}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/2.1.30'),
    'EN',
    '',
    '["Newton''s Laws"]',
    '[{"code": "a", "text": "What force must be applied to make the body slide off the board? How long will it take for the body to slide off if a force $F_{0}$ is applied to the board and the length of the board is $l$?"},
      {"code": "b", "text": "What are the accelerations of the body and the board if force $F_{0}$ acts on the body of mass $m_{1}$? (A body of mass $m_{1}$ lies on a board of mass $m_{2}$ resting on a smooth horizontal surface. The coefficient of friction between the body and the board is $\\mu$.)"}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.3 Oscillations and Waves — 3.2.7 (pendulum with magnet; 2 subtasks + problem & answer image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '3.2.7', 'savchenko/3.2.7', false,
    '["3.2.7.jpg"]',
    '2025-08-20T09:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/3.2.7'),
    'RU',
    '',
    '["Период и частота свободных колебаний"]',
    '[{"code": "а", "text": "Математический маятник — железный шарик массы $m$, висящий на длинной нити, — имеет период $T_{0}$. В присутствии магнита, расположенного чуть ниже шарика, период колебаний стал равным $T$. Определите действующую на шарик магнитную силу."},
      {"code": "б", "text": "Железный шарик маятника поместили между полюсами магнита так, что на него действует горизонтальная магнитная сила. Найдите эту силу и новое положение равновесия шарика, если период его колебаний после включения магнитного поля стал равным $T$."}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/3.2.7'),
    'EN',
    '',
    '["Period and frequency of free oscillations"]',
    '[{"code": "a", "text": "A simple pendulum — an iron ball of mass $m$ hanging on a long string — has period $T_{0}$. In the presence of a magnet placed just below the ball, the oscillation period became $T$. Determine the magnetic force acting on the ball."},
      {"code": "b", "text": "The iron ball of the pendulum was placed between the poles of a magnet so that a horizontal magnetic force acts on it. Find this force and the new equilibrium position of the ball if the oscillation period after switching on the magnetic field became $T$."}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.4 Fluid Mechanics — 4.3.12 (hard, triangular notch outflow; has image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '4.3.12', 'savchenko/4.3.12', true,
    '["4.3.12.jpg"]',
    '2025-09-10T11:15:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/4.3.12'),
    'RU',
    'Вода вытекает из широкого сосуда через треугольный вырез в его стенке. Во сколько раз уменьшится скорость понижения уровня воды при изменении высоты её уровня от $H$ до $h$?',
    '["Движение идеальной жидкости"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/4.3.12'),
    'EN',
    'Water flows from a wide vessel through a triangular notch in its wall. How many times does the rate of decrease of the water level decrease as the water level drops from $H$ to $h$?',
    '["Motion of an ideal fluid"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.5 Molecular Physics — 5.6.19 (adiabatic piston compression; has image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '5.6.19', 'savchenko/5.6.19', false,
    '["5.6.19.jpg"]',
    '2025-10-05T16:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/5.6.19'),
    'RU',
    'Поршень массы $M$, закрывающий объём $V_{0}$ одноатомного газа при давлении $P_{0}$ и температуре $T_{0}$, движется со скоростью $u$. Определите температуру и объём газа при максимальном сжатии. Система теплоизолирована, теплоёмкостями поршня и сосуда пренебречь.',
    '["Первое начало термодинамики. Теплоёмкость"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/5.6.19'),
    'EN',
    'A piston of mass $M$ enclosing a volume $V_{0}$ of a monatomic gas at pressure $P_{0}$ and temperature $T_{0}$ moves with speed $u$. Determine the temperature and volume of the gas at maximum compression. The system is thermally insulated; neglect the heat capacities of the piston and vessel.',
    '["First law of thermodynamics. Heat capacity"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.6 Electrostatics — 6.4.2 (parallel-plate capacitor; 2 subtasks)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '6.4.2', 'savchenko/6.4.2', false,
    '[]',
    '2025-11-12T08:30:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/6.4.2'),
    'RU',
    '',
    '["Конденсаторы"]',
    '[{"code": "а", "text": "Размеры пластин плоского конденсатора увеличили в два раза. Как изменилась ёмкость конденсатора?"},
      {"code": "б", "text": "Как изменится ёмкость плоского конденсатора, если расстояние между пластинами удвоить? Увеличить в $n$ раз?"}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/6.4.2'),
    'EN',
    '',
    '["Capacitors"]',
    '[{"code": "a", "text": "The dimensions of the plates of a parallel-plate capacitor were doubled. How did the capacitance change?"},
      {"code": "b", "text": "How does the capacitance of a parallel-plate capacitor change if the distance between the plates is doubled? Increased $n$ times?"}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.7 Charged Particle Motion — 7.4.10 (hard, charged particles near metallic dihedral; problem & answer image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '7.4.10', 'savchenko/7.4.10', true,
    '["7.4.10.jpg"]',
    '2025-12-01T13:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/7.4.10'),
    'RU',
    'Скорости трёх заряженных частиц массы $m$ изображены на рисунке. Расстояние от каждой частицы до ребра металлического двугранного угла $d$. Заряды первых двух частиц, летящих в противоположных направлениях, равны $\pm q$. Найдите скорость третьей нейтральной частицы на бесконечности, если начальная скорость этой частицы равна $v$.',
    '["Взаимодействие заряженных частиц"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/7.4.10'),
    'EN',
    'The velocities of three charged particles of mass $m$ are shown in the figure. The distance from each particle to the edge of a metallic dihedral angle is $d$. The charges of the first two particles, moving in opposite directions, are $\pm q$. Find the speed of the third neutral particle at infinity if its initial speed is $v$.',
    '["Interaction of charged particles"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.8 Electric Current — 8.3.8 (voltmeter range switching paradox)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '8.3.8', 'savchenko/8.3.8', false,
    '[]',
    '2026-01-08T10:45:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/8.3.8'),
    'RU',
    'Переключая вольтметр на измерение вдвое большего диапазона напряжения (со $100$ на $200$ В), ожидали отклонения стрелки на вдвое меньшее число делений. Однако этого не произошло, хотя в остальной части цепи ничего не изменяли. Большее или меньшее напряжение покажет вольтметр после переключения?',
    '["Электрические цепи"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/8.3.8'),
    'EN',
    'When switching a voltmeter to measure twice the voltage range (from $100$ to $200$ V), it was expected that the pointer would deflect to half as many divisions. However, this did not happen, although nothing else in the circuit was changed. Will the voltmeter show a higher or lower voltage after switching?',
    '["Electric circuits"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.9 Magnetic Field — 9.2.23 (magnetised plates, magnetic moment estimate)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '9.2.23', 'savchenko/9.2.23', false,
    '[]',
    '2026-02-14T15:20:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/9.2.23'),
    'RU',
    'Сила взаимодействия двух тонких намагниченных квадратных пластин, расположенных на расстоянии $H$ друг над другом, равна $F$. Размеры пластин $a \times a \times h$. Оцените магнитный момент единицы объёма пластины, если толщина пластины $h \ll H$, а $H \ll a$.',
    '["Магнитное поле движущегося заряда. Индукция магнитного поля линейного тока"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/9.2.23'),
    'EN',
    'The interaction force between two thin magnetised square plates placed at a distance $H$ above each other is $F$. The dimensions of the plates are $a \times a \times h$. Estimate the magnetic moment per unit volume of a plate, given that the plate thickness $h \ll H$ and $H \ll a$.',
    '["Magnetic field of a moving charge. Magnetic flux density of a linear current"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.10 Charged Particles — 10.1.21 (charged ring in axial magnetic field; has image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '10.1.21', 'savchenko/10.1.21', false,
    '["10.1.21.jpg"]',
    '2026-03-01T12:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/10.1.21'),
    'RU',
    'Равномерно заряженное кольцо радиуса $R$, линейная плотность заряда которого $\rho$, движется соосно аксиально-симметричному магнитному полю со скоростью $v$. Радиальная составляющая индукции магнитного поля на расстоянии $R$ от оси равна $B_{R}$. Определите момент сил, действующих на кольцо.',
    '["Движение в однородном магнитном поле"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/10.1.21'),
    'EN',
    'A uniformly charged ring of radius $R$ with linear charge density $\rho$ moves coaxially in an axially symmetric magnetic field at speed $v$. The radial component of the magnetic flux density at distance $R$ from the axis is $B_{R}$. Determine the torque acting on the ring.',
    '["Motion in a uniform magnetic field"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.11 Electromagnetic Induction — 11.3.19 (hard, transformer short-circuit; 2 subtasks)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '11.3.19', 'savchenko/11.3.19', true,
    '[]',
    '2026-03-18T09:30:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/11.3.19'),
    'RU',
    '',
    '["Взаимная индуктивность. Индуктивность проводников. Трансформаторы"]',
    '[{"code": "а", "text": "Почему опасно замыкание хотя бы одного витка вторичной обмотки трансформатора?"},
      {"code": "б", "text": "Замыкание витка вторичной обмотки трансформатора приводит иногда к выходу из строя первичной обмотки трансформатора. Почему это происходит?"}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/11.3.19'),
    'EN',
    '',
    '["Mutual inductance. Inductance of conductors. Transformers"]',
    '[{"code": "a", "text": "Why is the short-circuiting of even a single turn of the secondary winding of a transformer dangerous?"},
      {"code": "b", "text": "The short-circuiting of a turn of the secondary winding of a transformer sometimes causes failure of the primary winding. Why does this happen?"}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.12 Electromagnetic Waves — 12.1.25 (reflection law proof; 2 subtasks + image)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '12.1.25', 'savchenko/12.1.25', false,
    '["12.1.25.jpg"]',
    '2026-04-02T17:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/12.1.25'),
    'RU',
    'Пользуясь методом, изложенным в задаче 12.1.19, докажите, что угол падения электромагнитной волны равен углу отражения. Рассмотрите случаи:',
    '["Свойства, излучение и отражение электромагнитных волн"]',
    '[{"code": "а", "text": "вектор $E$ электромагнитной волны, падающей на металл, параллелен металлической поверхности;"},
      {"code": "б", "text": "вектор $B$ электромагнитной волны параллелен металлической поверхности."}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/12.1.25'),
    'EN',
    'Using the method described in problem 12.1.19, prove that the angle of incidence of an electromagnetic wave equals the angle of reflection. Consider the cases:',
    '["Properties, emission and reflection of electromagnetic waves"]',
    '[{"code": "a", "text": "the electric field vector $E$ of the wave incident on the metal is parallel to the metallic surface;"},
      {"code": "b", "text": "the magnetic field vector $B$ of the wave is parallel to the metallic surface."}]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.13 Optics — 13.3.10 (Moon photography defocus correction)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '13.3.10', 'savchenko/13.3.10', false,
    '[]',
    '2026-04-10T11:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/13.3.10'),
    'RU',
    'При фотографировании Луны получено размытое изображение в виде диска радиуса $r_{1}$. Резкое изображение Луны имело бы радиус $r_{2}$. Определите, на какое расстояние нужно сместить фотопластинку, чтобы изображение на ней получилось резким. Фокусное расстояние линзы $f$, диаметр $D$, при этом $r_{2} > D/2 > r_{1}$.',
    '["Оптические системы"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/13.3.10'),
    'EN',
    'When photographing the Moon, a blurred image in the form of a disk of radius $r_{1}$ was obtained. A sharp image of the Moon would have radius $r_{2}$. Determine by how much the photographic plate must be shifted to obtain a sharp image. The focal length of the lens is $f$, its diameter is $D$, with $r_{2} > D/2 > r_{1}$.',
    '["Optical systems"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- Ch.14 Special Relativity — 14.3.19 (relativistic electron beam density)
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '14.3.19', 'savchenko/14.3.19', false,
    '[]',
    '2026-04-20T14:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/14.3.19'),
    'RU',
    'Скорость электронов в параллельном пучке $\beta c$. Как изменится плотность электронов при движении относительно пучка со скоростью $\beta_{1}c$ в продольном направлении?',
    '["Преобразование электрического и магнитного полей"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/14.3.19'),
    'EN',
    'The speed of electrons in a parallel beam is $\beta c$. How does the electron density change when moving relative to the beam at speed $\beta_{1}c$ in the longitudinal direction?',
    '["Transformation of electric and magnetic fields"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- ── Answers ───────────────────────────────────────────────────────────────────

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/1.3.13'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/1.3.13')),
    'RU',
    'а) $\operatorname{tg}\varphi = \dfrac{v^{2} \pm \sqrt{v^{4} - 2gv^{2}y - g^{2}x^{2}}}{gx}$. б) $y = \dfrac{v^{2}}{2g} - \dfrac{gx^{2}}{2v^{2}}$. в) $v_{\text{мин}} = \sqrt{g\bigl(y + \sqrt{x^{2} + y^{2}}\bigr)}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/1.3.13')),
    'EN',
    'a) $\tan\varphi = \dfrac{v^{2} \pm \sqrt{v^{4} - 2gv^{2}y - g^{2}x^{2}}}{gx}$. b) $y = \dfrac{v^{2}}{2g} - \dfrac{gx^{2}}{2v^{2}}$. c) $v_{\min} = \sqrt{g\bigl(y + \sqrt{x^{2} + y^{2}}\bigr)}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/2.4.34'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/2.4.34')),
    'RU',
    '$Q = \dfrac{m_{1}gh(m_{1} - m_{2})}{m_{1} + m_{2}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/2.4.34')),
    'EN',
    '$Q = \dfrac{m_{1}gh(m_{1} - m_{2})}{m_{1} + m_{2}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/2.1.30'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/2.1.30')),
    'RU',
    'а) $F > \mu(m_{2} + m_{1})g$; $t = \sqrt{\dfrac{2lm_{2}}{F_{0} - \mu(m_{2} + m_{1})g}}$. б) $a_{1} = \dfrac{F_{0} - \mu m_{1}g}{m_{1}}$, $a_{2} = \mu g\dfrac{m_{1}}{m_{2}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/2.1.30')),
    'EN',
    'a) $F > \mu(m_{2} + m_{1})g$; $t = \sqrt{\dfrac{2lm_{2}}{F_{0} - \mu(m_{2} + m_{1})g}}$. b) $a_{1} = \dfrac{F_{0} - \mu m_{1}g}{m_{1}}$, $a_{2} = \mu g\dfrac{m_{1}}{m_{2}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/3.2.7'),
     '["3.2.7.jpg"]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/3.2.7')),
    'RU',
    'а) $F = mg\left[(T_{0}/T)^{2} - 1\right]$. б) $F = mg\sqrt{(T_{0}/T)^{4} - 1}$; $\cos\varphi = (T/T_{0})^{2}$.',
    '$\varphi$ — угол отклонения нового положения равновесия от вертикали.'
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/3.2.7')),
    'EN',
    'a) $F = mg\left[(T_{0}/T)^{2} - 1\right]$. b) $F = mg\sqrt{(T_{0}/T)^{4} - 1}$; $\cos\varphi = (T/T_{0})^{2}$.',
    '$\varphi$ is the angle of deflection of the new equilibrium position from the vertical.'
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/4.3.12'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/4.3.12')),
    'RU',
    'Скорость понижения уровня уменьшится в $\left(\dfrac{H}{h}\right)^{5/2}$ раз.',
    'При понижении уровня ширина выреза уменьшается в $H/h$ раз, скорость истечения — в $\sqrt{H/h}$ раз; скорость понижения уровня пропорциональна их произведению: $(H/h)^{2}\cdot\sqrt{H/h}$.'
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/4.3.12')),
    'EN',
    'The rate of decrease of the level decreases by $\left(\dfrac{H}{h}\right)^{5/2}$ times.',
    'As the level drops, the notch width decreases by $H/h$, the outflow speed by $\sqrt{H/h}$; the rate of level decrease is proportional to their product: $(H/h)^{2}\cdot\sqrt{H/h}$.'
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/5.6.19'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/5.6.19')),
    'RU',
    '$T = T_{0}\!\left(1 + \dfrac{Mu^{2}}{3P_{0}V_{0}}\right)$, $V = V_{0}\!\left(\dfrac{3P_{0}V_{0}}{3P_{0}V_{0}+Mu^{2}}\right)^{\!3/2}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/5.6.19')),
    'EN',
    '$T = T_{0}\!\left(1 + \dfrac{Mu^{2}}{3P_{0}V_{0}}\right)$, $V = V_{0}\!\left(\dfrac{3P_{0}V_{0}}{3P_{0}V_{0}+Mu^{2}}\right)^{\!3/2}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/6.4.2'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/6.4.2')),
    'RU',
    'а) Увеличилась в четыре раза. б) Уменьшится в два раза; уменьшится в $n$ раз.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/6.4.2')),
    'EN',
    'a) Increased four times. b) Decreases two times; decreases $n$ times.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/7.4.10'),
     '["7.4.10.jpg"]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/7.4.10')),
    'RU',
    '$v_{\infty} = \sqrt{v^{2} + \dfrac{q^{2}(\sqrt{2}-1)}{4\pi\varepsilon_{0}md}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/7.4.10')),
    'EN',
    '$v_{\infty} = \sqrt{v^{2} + \dfrac{q^{2}(\sqrt{2}-1)}{4\pi\varepsilon_{0}md}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/8.3.8'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/8.3.8')),
    'RU',
    'Большее.',
    'При увеличении диапазона внутреннее сопротивление вольтметра возрастает; ток через цепь уменьшается меньше, чем в два раза, поэтому показание оказывается больше ожидаемого.'
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/8.3.8')),
    'EN',
    'Higher.',
    'When the range is increased, the internal resistance of the voltmeter increases; the current through the circuit decreases by less than a factor of two, so the reading is higher than expected.'
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/9.2.23'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/9.2.23')),
    'RU',
    '$M = \sqrt{\dfrac{\pi H F}{2\mu_{0} a h^{2}}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/9.2.23')),
    'EN',
    '$M = \sqrt{\dfrac{\pi H F}{2\mu_{0} a h^{2}}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/10.1.21'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/10.1.21')),
    'RU',
    '$N = 2\pi R^{2}\rho v B_{R}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/10.1.21')),
    'EN',
    '$N = 2\pi R^{2}\rho v B_{R}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

-- Ch.1 Kinematics — 1.1.4 (pion decay, speed of light; has image) — added for submission testing
INSERT INTO problems (book_id, code, key, is_hard, images, created_at)
VALUES (
    (SELECT id FROM books WHERE slug = 'savchenko'),
    '1.1.4', 'savchenko/1.1.4', false,
    '["1.1.4.jpg"]',
    '2026-04-25T08:00:00Z'
) ON CONFLICT (key) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'),
    'RU',
    'Счетчики $A$ и $B$, регистрирующие момент прихода $\gamma$-кванта, расположены на расстоянии $2$ м друг от друга. В некоторой точке между ними произошел распад $\pi^{0}$-мезона на два $\gamma$-кванта. Найдите положение этой точки, если счетчик $A$ зарегистрировал $\gamma$-квант на $10^{-9}$ с позднее, чем счетчик $B$. Скорость света $3 \cdot 10^{8}$ м/с.',
    '["Движение с постоянной скоростью"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

INSERT INTO problem_localizations (problem_id, language, text, tags, subproblems)
VALUES (
    (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'),
    'EN',
    'Counters $A$ and $B$, which record the arrival time of $\gamma$-quanta, are placed $2$ m apart. At some point between them a $\pi^{0}$-meson decayed into two $\gamma$-quanta. Find the position of this point if counter $A$ registered a $\gamma$-quantum $10^{-9}$ s later than counter $B$. The speed of light is $3 \cdot 10^{8}$ m/s.',
    '["Motion at constant velocity"]',
    '[]'
) ON CONFLICT (problem_id, language) DO NOTHING;

-- 11.3.19 — no answer in source data, intentionally omitted.

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/12.1.25'),
     '["12.1.25.jpg"]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/12.1.25')),
    'RU',
    'а) $E'' = -E$, $B'' = B$. б) $E'' = E$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/12.1.25')),
    'EN',
    'a) $E'' = -E$, $B'' = B$. b) $E'' = E$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/13.3.10'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/13.3.10')),
    'RU',
    'К линзе на расстояние $\Delta l = \dfrac{r_{1} - r_{2}}{D/2 + r_{2}}\,f$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/13.3.10')),
    'EN',
    'Move the photographic plate towards the lens by $\Delta l = \dfrac{r_{1} - r_{2}}{D/2 + r_{2}}\,f$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/14.3.19'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/14.3.19')),
    'RU',
    'Увеличится в $\dfrac{1 + \beta\beta_{1}}{\sqrt{1 - \beta_{1}^{2}}}$ раз.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/14.3.19')),
    'EN',
    'Increases by a factor of $\dfrac{1 + \beta\beta_{1}}{\sqrt{1 - \beta_{1}^{2}}}$.',
    ''
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answers (problem_id, images) VALUES
    ((SELECT id FROM problems WHERE key = 'savchenko/1.1.4'),
     '[]')
ON CONFLICT (problem_id) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/1.1.4')),
    'RU',
    'На расстоянии $1{,}15$ м от счётчика $A$.',
    null
) ON CONFLICT (answer_id, language) DO NOTHING;

INSERT INTO answer_localizations (answer_id, language, text, notes)
VALUES (
    (SELECT id FROM answers WHERE problem_id = (SELECT id FROM problems WHERE key = 'savchenko/1.1.4')),
    'EN',
    'At a distance of $1.15$ m from counter $A$.',
    null
) ON CONFLICT (answer_id, language) DO NOTHING;

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

INSERT INTO submissions (id, problem_id, user_id, status, language, file_object_ids, created_at, updated_at)
VALUES (
    1,
    (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'),
    (SELECT id FROM users WHERE name = 'admin'),
    'PENDING',
    'RU',
    '[1]',
    '2026-04-23 09:00:00+00',
    '2026-04-23 09:00:00+00'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO submissions (id, problem_id, user_id, status, language, file_object_ids, created_at, updated_at)
VALUES
    (2, (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'), (SELECT id FROM users WHERE name = 'admin'), 'PENDING', 'RU', '[]', '2026-04-23 10:00:00+00', '2026-04-23 10:00:00+00'),
    (3, (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'), (SELECT id FROM users WHERE name = 'admin'), 'PENDING', 'RU', '[]', '2026-04-23 11:00:00+00', '2026-04-23 11:00:00+00'),
    (4, (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'), (SELECT id FROM users WHERE name = 'admin'), 'PENDING', 'RU', '[]', '2026-04-23 12:00:00+00', '2026-04-23 12:00:00+00')
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

-- ── Submissions by all three users across many problems ───────────────────────
-- 15 total submissions (4 admin above + 5 moderator + 6 user) with varied
-- statuses, problems, and file attachments for /submissions search page testing.
--
-- Submission file objects (put these files in MinIO bucket edukate-bucket):
-- ┌────┬──────────────────────────────────────────────────────┬──────────┐
-- │ id │ S3 key (MinIO)                                       │ user     │
-- ├────┼──────────────────────────────────────────────────────┼──────────┤
-- │  2 │ users/2/submissions/2/5/solution.jpg                  │ moderator│
-- │  3 │ users/2/submissions/5/7/scan.jpg                      │ moderator│
-- │  4 │ users/2/submissions/8/9/diagram.jpg                   │ moderator│
-- │  5 │ users/3/submissions/16/10/solution.jpg                │ user     │
-- │  6 │ users/3/submissions/4/11/page1.jpg                    │ user     │
-- │  7 │ users/3/submissions/4/11/page2.jpg                    │ user     │
-- │  8 │ users/3/submissions/1/13/attempt.jpg                  │ user     │
-- │  9 │ users/3/submissions/11/15/solution.jpg                │ user     │
-- └────┴──────────────────────────────────────────────────────┴──────────┘

INSERT INTO file_objects (id, key_path, key, type, owner_user_id, metadata, created_at, updated_at) VALUES
    -- moderator files
    (2,
     'users/2/submissions/2/5/solution.jpg',
     '{"_type": "submission", "userId": 2, "problemId": 2, "submissionId": 5, "fileName": "solution.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'moderator'),
     '{"lastModified": "2026-04-24T14:00:00Z", "contentLength": 52340, "contentType": "image/jpeg"}',
     '2026-04-24 14:00:00+00', '2026-04-24 14:00:00+00'),
    (3,
     'users/2/submissions/5/7/scan.jpg',
     '{"_type": "submission", "userId": 2, "problemId": 5, "submissionId": 7, "fileName": "scan.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'moderator'),
     '{"lastModified": "2026-04-24T16:00:00Z", "contentLength": 67100, "contentType": "image/jpeg"}',
     '2026-04-24 16:00:00+00', '2026-04-24 16:00:00+00'),
    (4,
     'users/2/submissions/8/9/diagram.jpg',
     '{"_type": "submission", "userId": 2, "problemId": 8, "submissionId": 9, "fileName": "diagram.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'moderator'),
     '{"lastModified": "2026-04-24T18:00:00Z", "contentLength": 43200, "contentType": "image/jpeg"}',
     '2026-04-24 18:00:00+00', '2026-04-24 18:00:00+00'),
    -- user files
    (5,
     'users/3/submissions/16/10/solution.jpg',
     '{"_type": "submission", "userId": 3, "problemId": 16, "submissionId": 10, "fileName": "solution.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'user'),
     '{"lastModified": "2026-04-25T09:00:00Z", "contentLength": 38910, "contentType": "image/jpeg"}',
     '2026-04-25 09:00:00+00', '2026-04-25 09:00:00+00'),
    (6,
     'users/3/submissions/4/11/page1.jpg',
     '{"_type": "submission", "userId": 3, "problemId": 4, "submissionId": 11, "fileName": "page1.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'user'),
     '{"lastModified": "2026-04-25T10:00:00Z", "contentLength": 71200, "contentType": "image/jpeg"}',
     '2026-04-25 10:00:00+00', '2026-04-25 10:00:00+00'),
    (7,
     'users/3/submissions/4/11/page2.jpg',
     '{"_type": "submission", "userId": 3, "problemId": 4, "submissionId": 11, "fileName": "page2.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'user'),
     '{"lastModified": "2026-04-25T10:00:00Z", "contentLength": 63450, "contentType": "image/jpeg"}',
     '2026-04-25 10:00:00+00', '2026-04-25 10:00:00+00'),
    (8,
     'users/3/submissions/1/13/attempt.jpg',
     '{"_type": "submission", "userId": 3, "problemId": 1, "submissionId": 13, "fileName": "attempt.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'user'),
     '{"lastModified": "2026-04-25T14:00:00Z", "contentLength": 55600, "contentType": "image/jpeg"}',
     '2026-04-25 14:00:00+00', '2026-04-25 14:00:00+00'),
    (9,
     'users/3/submissions/11/15/solution.jpg',
     '{"_type": "submission", "userId": 3, "problemId": 11, "submissionId": 15, "fileName": "solution.jpg"}',
     'submission',
     (SELECT id FROM users WHERE name = 'user'),
     '{"lastModified": "2026-04-26T09:00:00Z", "contentLength": 48300, "contentType": "image/jpeg"}',
     '2026-04-26 09:00:00+00', '2026-04-26 09:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- ── Moderator submissions (5 subs: ids 5–9) ─────────────────────────────────
--   Sub  5: moderator → 2.4.34  (solution.jpg) → SUCCESS
--   Sub  6: moderator → 1.3.13  (no files)     → PENDING
--   Sub  7: moderator → 4.3.12  (scan.jpg)     → FAILED
--   Sub  8: moderator → 5.6.19  (no files)     → SUCCESS
--   Sub  9: moderator → 7.4.10  (diagram.jpg)  → FAILED

INSERT INTO submissions (id, problem_id, user_id, status, language, file_object_ids, created_at, updated_at)
VALUES
    (5,  (SELECT id FROM problems WHERE key = 'savchenko/2.4.34'), (SELECT id FROM users WHERE name = 'moderator'), 'PENDING', 'RU', '["2"]',  '2026-04-24 14:00:00+00', '2026-04-24 14:00:00+00'),
    (6,  (SELECT id FROM problems WHERE key = 'savchenko/1.3.13'), (SELECT id FROM users WHERE name = 'moderator'), 'PENDING', 'RU', '[]',     '2026-04-24 15:00:00+00', '2026-04-24 15:00:00+00'),
    (7,  (SELECT id FROM problems WHERE key = 'savchenko/4.3.12'), (SELECT id FROM users WHERE name = 'moderator'), 'PENDING', 'RU', '["3"]',  '2026-04-24 16:00:00+00', '2026-04-24 16:00:00+00'),
    (8,  (SELECT id FROM problems WHERE key = 'savchenko/5.6.19'), (SELECT id FROM users WHERE name = 'moderator'), 'PENDING', 'RU', '[]',     '2026-04-24 17:00:00+00', '2026-04-24 17:00:00+00'),
    (9,  (SELECT id FROM problems WHERE key = 'savchenko/7.4.10'), (SELECT id FROM users WHERE name = 'moderator'), 'PENDING', 'RU', '["4"]',  '2026-04-24 18:00:00+00', '2026-04-24 18:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- ── User submissions (6 subs: ids 10–15) ────────────────────────────────────
--   Sub 10: user → 1.1.4   (solution.jpg)          → FAILED
--   Sub 11: user → 3.2.7   (page1.jpg + page2.jpg) → SUCCESS
--   Sub 12: user → 6.4.2   (no files)              → PENDING
--   Sub 13: user → 1.3.13  (attempt.jpg)           → FAILED
--   Sub 14: user → 2.4.34  (no files)              → SUCCESS
--   Sub 15: user → 10.1.21 (solution.jpg)          → PENDING

INSERT INTO submissions (id, problem_id, user_id, status, language, file_object_ids, created_at, updated_at)
VALUES
    (10, (SELECT id FROM problems WHERE key = 'savchenko/1.1.4'),  (SELECT id FROM users WHERE name = 'user'), 'PENDING', 'RU', '["5"]',     '2026-04-25 09:00:00+00', '2026-04-25 09:00:00+00'),
    (11, (SELECT id FROM problems WHERE key = 'savchenko/3.2.7'),  (SELECT id FROM users WHERE name = 'user'), 'PENDING', 'RU', '["6","7"]', '2026-04-25 10:00:00+00', '2026-04-25 10:00:00+00'),
    (12, (SELECT id FROM problems WHERE key = 'savchenko/6.4.2'),  (SELECT id FROM users WHERE name = 'user'), 'PENDING', 'RU', '[]',        '2026-04-25 12:00:00+00', '2026-04-25 12:00:00+00'),
    (13, (SELECT id FROM problems WHERE key = 'savchenko/1.3.13'), (SELECT id FROM users WHERE name = 'user'), 'PENDING', 'RU', '["8"]',     '2026-04-25 14:00:00+00', '2026-04-25 14:00:00+00'),
    (14, (SELECT id FROM problems WHERE key = 'savchenko/2.4.34'), (SELECT id FROM users WHERE name = 'user'), 'PENDING', 'RU', '[]',        '2026-04-25 16:00:00+00', '2026-04-25 16:00:00+00'),
    (15, (SELECT id FROM problems WHERE key = 'savchenko/10.1.21'),(SELECT id FROM users WHERE name = 'user'), 'PENDING', 'RU', '["9"]',     '2026-04-26 09:00:00+00', '2026-04-26 09:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- ── Check results for submissions 5–15 ──────────────────────────────────────
-- Triggers fire: fn_sync_submission_status → update status + updated_at,
--                fn_update_problem_progress → upsert progress.
-- Subs 6, 12, 15 have no non-PENDING check results → stay PENDING.

INSERT INTO check_results (id, submission_id, status, trust_level, error_type, explanation, created_at)
VALUES
    -- moderator results
    (5,  5,  'SUCCESS', 0.92, 'NONE',
        'Correct application of energy conservation and inelastic collision conditions.',
        '2026-04-24 14:10:00+00'),
    (6,  7,  'MISTAKE', 0.35, 'ALGEBRAIC',
        'The ratio H/h was applied to velocity instead of the square root; exponent should be 5/2, not 3/2.',
        '2026-04-24 16:15:00+00'),
    (7,  8,  'SUCCESS', 0.90, 'NONE',
        'Correct thermodynamic analysis. Temperature and volume expressions match expected results.',
        '2026-04-24 17:12:00+00'),
    (8,  9,  'INTERNAL_ERROR', 0.0, 'UNCLEAR',
        'Diagram too blurry to parse; please re-upload a higher resolution scan.',
        '2026-04-24 18:08:00+00'),
    -- user results
    (9,  10, 'MISTAKE', 0.45, 'ALGEBRAIC',
        'The distance was calculated from the wrong counter; the answer should be 1.15 m from A, not B.',
        '2026-04-25 09:20:00+00'),
    (10, 11, 'SUCCESS', 0.88, 'NONE',
        'Both subtasks solved correctly. Magnetic force and equilibrium angle match expected values.',
        '2026-04-25 10:25:00+00'),
    (11, 13, 'MISTAKE', 0.40, 'ALGEBRAIC',
        'The envelope equation is correct, but the minimum velocity formula has a sign error under the radical.',
        '2026-04-25 14:18:00+00'),
    (12, 14, 'SUCCESS', 0.94, 'NONE',
        'Clean solution. Energy balance and inelastic collision heat loss correctly derived.',
        '2026-04-25 16:10:00+00')
ON CONFLICT (id) DO NOTHING;

-- Advance sequences past the hardcoded IDs so future inserts don't collide.
SELECT setval('file_objects_id_seq',  GREATEST((SELECT MAX(id) FROM file_objects),  1));
SELECT setval('submissions_id_seq',   GREATEST((SELECT MAX(id) FROM submissions),   1));
SELECT setval('check_results_id_seq', GREATEST((SELECT MAX(id) FROM check_results), 1));
SELECT setval('problem_sets_id_seq',  GREATEST((SELECT MAX(id) FROM problem_sets),  1));

-- ── Problem sets ──────────────────────────────────────────────────────────────
-- Three problem sets covering every interesting membership scenario:
--
--   Set 1 "Kinematics Practice" — admin owns it; user is a member
--   Set 2 "Classical Mechanics"  — moderator owns it; admin is USER, user is USER
--   Set 3 "Intro to Physics"     — public; moderator owns it; admin + user are members
--
-- UserRole values (problem-set level): ADMIN · USER · MODERATOR

INSERT INTO problem_sets (id, name, description, is_public, share_code, user_id_role_map, invited_user_ids) VALUES (
    1,
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
) ON CONFLICT (id) DO NOTHING;

INSERT INTO problem_sets (id, name, description, is_public, share_code, user_id_role_map, invited_user_ids) VALUES (
    2,
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
) ON CONFLICT (id) DO NOTHING;

INSERT INTO problem_sets (id, name, description, is_public, share_code, user_id_role_map, invited_user_ids) VALUES (
    3,
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
) ON CONFLICT (id) DO NOTHING;

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
