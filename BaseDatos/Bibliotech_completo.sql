--
-- PostgreSQL database dump
--

\restrict PhTYhKarcUj0hD28byTfdE1iPXbmILDQdEv7JTHjb9CaDJNogdNi1MUwGawD5TZ

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.0

-- Started on 2026-09-26 17:47:27

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 219 (class 1259 OID 26452)
-- Name: estudiante; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.estudiante (
    cedula character varying(20) NOT NULL,
    nombre character varying(50),
    apellido character varying(50)
);


ALTER TABLE public.estudiante OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 26456)
-- Name: libro; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.libro (
    id integer NOT NULL,
    isbn character varying(20),
    titulo character varying(100),
    autor character varying(100),
    cantidad_total integer
);


ALTER TABLE public.libro OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 26460)
-- Name: libro_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.libro_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.libro_id_seq OWNER TO postgres;

--
-- TOC entry 5049 (class 0 OID 0)
-- Dependencies: 221
-- Name: libro_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.libro_id_seq OWNED BY public.libro.id;


--
-- TOC entry 225 (class 1259 OID 26487)
-- Name: prestamo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prestamo (
    id integer NOT NULL,
    id_libro integer NOT NULL,
    cedula_estudiante character varying(20) NOT NULL,
    fecha_inicio date DEFAULT CURRENT_DATE NOT NULL,
    fecha_limite_devolucion date NOT NULL,
    fecha_devolucion date,
    estado character varying(10) DEFAULT 'ACTIVO'::character varying NOT NULL,
    CONSTRAINT prestamo_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'DEVUELTO'::character varying])::text[])))
);


ALTER TABLE public.prestamo OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 26486)
-- Name: prestamo_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.prestamo_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.prestamo_id_seq OWNER TO postgres;

--
-- TOC entry 5050 (class 0 OID 0)
-- Dependencies: 224
-- Name: prestamo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.prestamo_id_seq OWNED BY public.prestamo.id;


--
-- TOC entry 222 (class 1259 OID 26461)
-- Name: reserva; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reserva (
    id integer NOT NULL,
    fecha_inicio date,
    fecha_fin date,
    id_libro integer,
    cedula_estudiante character varying(20)
);


ALTER TABLE public.reserva OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 26465)
-- Name: reserva_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.reserva_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reserva_id_seq OWNER TO postgres;

--
-- TOC entry 5051 (class 0 OID 0)
-- Dependencies: 223
-- Name: reserva_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.reserva_id_seq OWNED BY public.reserva.id;


--
-- TOC entry 4870 (class 2604 OID 26466)
-- Name: libro id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.libro ALTER COLUMN id SET DEFAULT nextval('public.libro_id_seq'::regclass);


--
-- TOC entry 4872 (class 2604 OID 26490)
-- Name: prestamo id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prestamo ALTER COLUMN id SET DEFAULT nextval('public.prestamo_id_seq'::regclass);


--
-- TOC entry 4871 (class 2604 OID 26467)
-- Name: reserva id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva ALTER COLUMN id SET DEFAULT nextval('public.reserva_id_seq'::regclass);


--
-- TOC entry 5037 (class 0 OID 26452)
-- Dependencies: 219
-- Data for Name: estudiante; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.estudiante (cedula, nombre, apellido) FROM stdin;
1234567	Juan	Perez
\.


--
-- TOC entry 5038 (class 0 OID 26456)
-- Dependencies: 220
-- Data for Name: libro; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.libro (id, isbn, titulo, autor, cantidad_total) FROM stdin;
2	978	Redes de computadoras	Davalos	10
3	123	La Odisea	Joha	4
1	978-0132143011	Sistemas Distribuidos	George Coulouris	5
\.


--
-- TOC entry 5043 (class 0 OID 26487)
-- Dependencies: 225
-- Data for Name: prestamo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prestamo (id, id_libro, cedula_estudiante, fecha_inicio, fecha_limite_devolucion, fecha_devolucion, estado) FROM stdin;
1	2	1234567	2026-09-26	2026-10-03	\N	ACTIVO
\.


--
-- TOC entry 5040 (class 0 OID 26461)
-- Dependencies: 222
-- Data for Name: reserva; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reserva (id, fecha_inicio, fecha_fin, id_libro, cedula_estudiante) FROM stdin;
1	2026-09-12	2026-09-19	1	1234567
\.


--
-- TOC entry 5052 (class 0 OID 0)
-- Dependencies: 221
-- Name: libro_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.libro_id_seq', 3, true);


--
-- TOC entry 5053 (class 0 OID 0)
-- Dependencies: 224
-- Name: prestamo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.prestamo_id_seq', 1, true);


--
-- TOC entry 5054 (class 0 OID 0)
-- Dependencies: 223
-- Name: reserva_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reserva_id_seq', 1, true);


--
-- TOC entry 4877 (class 2606 OID 26469)
-- Name: estudiante estudiante_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.estudiante
    ADD CONSTRAINT estudiante_pkey PRIMARY KEY (cedula);


--
-- TOC entry 4879 (class 2606 OID 26471)
-- Name: libro libro_isbn_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.libro
    ADD CONSTRAINT libro_isbn_key UNIQUE (isbn);


--
-- TOC entry 4881 (class 2606 OID 26473)
-- Name: libro libro_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.libro
    ADD CONSTRAINT libro_pkey PRIMARY KEY (id);


--
-- TOC entry 4885 (class 2606 OID 26501)
-- Name: prestamo prestamo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prestamo
    ADD CONSTRAINT prestamo_pkey PRIMARY KEY (id);


--
-- TOC entry 4883 (class 2606 OID 26475)
-- Name: reserva reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (id);


--
-- TOC entry 4888 (class 2606 OID 26507)
-- Name: prestamo prestamo_cedula_estudiante_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prestamo
    ADD CONSTRAINT prestamo_cedula_estudiante_fkey FOREIGN KEY (cedula_estudiante) REFERENCES public.estudiante(cedula);


--
-- TOC entry 4889 (class 2606 OID 26502)
-- Name: prestamo prestamo_id_libro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prestamo
    ADD CONSTRAINT prestamo_id_libro_fkey FOREIGN KEY (id_libro) REFERENCES public.libro(id);


--
-- TOC entry 4886 (class 2606 OID 26476)
-- Name: reserva reserva_cedula_estudiante_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_cedula_estudiante_fkey FOREIGN KEY (cedula_estudiante) REFERENCES public.estudiante(cedula);


--
-- TOC entry 4887 (class 2606 OID 26481)
-- Name: reserva reserva_id_libro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_id_libro_fkey FOREIGN KEY (id_libro) REFERENCES public.libro(id);


-- Completed on 2026-09-26 17:47:27

--
-- PostgreSQL database dump complete
--

\unrestrict PhTYhKarcUj0hD28byTfdE1iPXbmILDQdEv7JTHjb9CaDJNogdNi1MUwGawD5TZ

