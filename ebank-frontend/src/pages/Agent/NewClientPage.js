import React, { useEffect, useMemo, useRef, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../auth/AuthContext';
import { useNavigate, Navigate } from 'react-router-dom';

const API_URL = 'http://localhost:8080/api/agents/clients';

const NewClientPage = () => {
  const { userRole } = useAuth();
  const navigate = useNavigate();

  const [clientData, setClientData] = useState({
    lastName: '',
    firstName: '',
    birthDate: '',
    postalAddress: '',
    email: '',
    identityNumber: '',
  });

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setClientData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    const requiredFields = ['lastName', 'firstName', 'identityNumber', 'email'];
    const missing = requiredFields.filter(field => !clientData[field]);

    if (missing.length > 0) {
      setError(`RG_5: Les champs suivants sont obligatoires : ${missing.join(', ')}`);
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await axios.post(API_URL, clientData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setMessage(response.data);
      setClientData({
        lastName: '',
        firstName: '',
        birthDate: '',
        postalAddress: '',
        email: '',
        identityNumber: '',
      });

    } catch (err) {
      console.error(err);

      if (err.response && (err.response.status === 409 || err.response.status === 400)) {
        setError("Échec : " + err.response.data);
      } else if (err.response && err.response.status === 403) {
        setError("RG : Accès interdit. Veuillez contacter l’administrateur.");
      } else {
        setError("Erreur serveur. Vérifiez les données ou la connexion.");
      }
    }
  };

  if (userRole !== 'AGENT_GUICHET') {
    return <Navigate to="/unauthorized" replace />;
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center px-4">
      <div className="w-full max-w-3xl bg-white rounded-2xl shadow-lg p-8">

        {/* ===== TITLE ===== */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">
            Création d’un nouveau client
          </h1>
          <p className="text-sm text-gray-500">
            Interface Agent Guichet
          </p>
        </div>

        {/* ===== ALERTS ===== */}
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-100 text-red-700 text-sm">
            {error}
          </div>
        )}

        {message && (
          <div className="mb-4 p-3 rounded-lg bg-green-100 text-green-700 text-sm">
            {message}
          </div>
        )}

        {/* ===== FORM ===== */}
        <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">

          <Input label="Nom" name="lastName" value={clientData.lastName} onChange={handleChange} />
          <Input label="Prénom" name="firstName" value={clientData.firstName} onChange={handleChange} />
          <Input label="Numéro d’identité" name="identityNumber" value={clientData.identityNumber} onChange={handleChange} />
          <DatePicker
            label="Date de naissance"
            value={clientData.birthDate}
            onChange={(iso) => setClientData(prev => ({ ...prev, birthDate: iso }))}
          />
          <Input label="Adresse email" type="email" name="email" value={clientData.email} onChange={handleChange} />
          <Input label="Adresse postale" name="postalAddress" value={clientData.postalAddress} onChange={handleChange} />

          {/* ===== SUBMIT ===== */}
          <div className="md:col-span-2 mt-4">
            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-3 rounded-xl font-semibold hover:bg-blue-700 transition"
            >
              Créer le client
            </button>
          </div>
        </form>


      </div>
    </div>
  );
};

/* ===== REUSABLE INPUT COMPONENT ===== */
const Input = ({ label, type = "text", ...props }) => (
  <div className="flex flex-col">
    <label className="text-sm font-medium text-gray-700 mb-1">
      {label}
    </label>
    <input
      type={type}
      className="px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
      {...props}
      required
    />
  </div>
);

const DatePicker = ({ label, value, onChange }) => {
  const wrapperRef = useRef(null);
  const [open, setOpen] = useState(false);

  const parsedValue = useMemo(() => {
    if (!value) return null;
    const dt = new Date(`${value}T00:00:00`);
    if (Number.isNaN(dt.getTime())) return null;
    return dt;
  }, [value]);

  const [viewYear, setViewYear] = useState(() => (parsedValue ? parsedValue.getFullYear() : new Date().getFullYear()));
  const [viewMonth, setViewMonth] = useState(() => (parsedValue ? parsedValue.getMonth() : new Date().getMonth()));

  useEffect(() => {
    if (!parsedValue) return;
    setViewYear(parsedValue.getFullYear());
    setViewMonth(parsedValue.getMonth());
  }, [parsedValue]);

  useEffect(() => {
    const handler = (e) => {
      if (!wrapperRef.current) return;
      if (!wrapperRef.current.contains(e.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const pad2 = (n) => String(n).padStart(2, '0');
  const toIso = (y, m0, d) => `${y}-${pad2(m0 + 1)}-${pad2(d)}`;
  const formatFr = (iso) => {
    if (!iso) return '';
    const [y, m, d] = iso.split('-');
    if (!y || !m || !d) return '';
    return `${d}/${m}/${y}`;
  };

  const monthNames = [
    'Janvier',
    'Février',
    'Mars',
    'Avril',
    'Mai',
    'Juin',
    'Juillet',
    'Août',
    'Septembre',
    'Octobre',
    'Novembre',
    'Décembre',
  ];

  const minYear = new Date().getFullYear() - 120;
  const maxYear = new Date().getFullYear();
  const maxMonthForMaxYear = new Date().getMonth();
  const minMonthForMinYear = 0;

  const years = useMemo(() => {
    const arr = [];
    for (let y = maxYear; y >= minYear; y -= 1) {
      arr.push(y);
    }
    return arr;
  }, [maxYear, minYear]);

  const daysInMonth = (y, m0) => new Date(y, m0 + 1, 0).getDate();
  const firstDayOffsetMonday = (y, m0) => {
    const dow = new Date(y, m0, 1).getDay();
    return (dow + 6) % 7;
  };

  const offset = firstDayOffsetMonday(viewYear, viewMonth);
  const dim = daysInMonth(viewYear, viewMonth);
  const cells = [];
  for (let i = 0; i < offset; i += 1) cells.push(null);
  for (let d = 1; d <= dim; d += 1) cells.push(d);
  while (cells.length < 42) cells.push(null);

  const selectedIso = value;
  const selectedDay = parsedValue ? parsedValue.getDate() : null;
  const selectedMonth = parsedValue ? parsedValue.getMonth() : null;
  const selectedYear = parsedValue ? parsedValue.getFullYear() : null;

  const goPrevMonth = () => {
    const next = new Date(viewYear, viewMonth - 1, 1);
    setViewYear(next.getFullYear());
    setViewMonth(next.getMonth());
  };

  const goNextMonth = () => {
    const next = new Date(viewYear, viewMonth + 1, 1);
    setViewYear(next.getFullYear());
    setViewMonth(next.getMonth());
  };

  const clampMonthForYear = (year, month) => {
    if (year === maxYear && month > maxMonthForMaxYear) return maxMonthForMaxYear;
    if (year === minYear && month < minMonthForMinYear) return minMonthForMinYear;
    return month;
  };

  const handleYearSelect = (e) => {
    const nextYear = Number(e.target.value);
    const nextMonth = clampMonthForYear(nextYear, viewMonth);
    setViewYear(nextYear);
    setViewMonth(nextMonth);
  };

  const handleMonthSelect = (e) => {
    const nextMonth = clampMonthForYear(viewYear, Number(e.target.value));
    setViewMonth(nextMonth);
  };

  const canGoPrev = viewYear > minYear || (viewYear === minYear && viewMonth > 0);
  const canGoNext = viewYear < maxYear || (viewYear === maxYear && viewMonth < 11);

  const handlePick = (day) => {
    const iso = toIso(viewYear, viewMonth, day);
    onChange(iso);
    setOpen(false);
  };

  const displayValue = selectedIso ? formatFr(selectedIso) : '';

  return (
    <div ref={wrapperRef} className="flex flex-col relative">
      <label className="text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        type="text"
        value={displayValue}
        readOnly
        placeholder="JJ/MM/AAAA"
        onClick={() => setOpen((v) => !v)}
        className="px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
        required
      />

      {open && (
        <div className="absolute z-20 mt-2 w-full bg-white border rounded-xl shadow-lg p-3">
          <div className="flex items-center justify-between mb-2">
            <button
              type="button"
              className="px-2 py-1 rounded hover:bg-gray-100 disabled:opacity-50"
              onClick={goPrevMonth}
              disabled={!canGoPrev}
            >
              {'<'}
            </button>

            <div className="flex items-center gap-2">
              <select
                className="px-2 py-1 border rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={viewMonth}
                onChange={handleMonthSelect}
              >
                {monthNames.map((m, idx) => {
                  const disabled =
                    (viewYear === maxYear && idx > maxMonthForMaxYear) ||
                    (viewYear === minYear && idx < minMonthForMinYear);
                  return (
                    <option key={m} value={idx} disabled={disabled}>
                      {m}
                    </option>
                  );
                })}
              </select>

              <select
                className="px-2 py-1 border rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={viewYear}
                onChange={handleYearSelect}
              >
                {years.map((y) => (
                  <option key={y} value={y}>
                    {y}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              className="px-2 py-1 rounded hover:bg-gray-100 disabled:opacity-50"
              onClick={goNextMonth}
              disabled={!canGoNext}
            >
              {'>'}
            </button>
          </div>

          <div className="grid grid-cols-7 gap-1 text-xs text-gray-500 mb-1">
            <div className="text-center">L</div>
            <div className="text-center">M</div>
            <div className="text-center">M</div>
            <div className="text-center">J</div>
            <div className="text-center">V</div>
            <div className="text-center">S</div>
            <div className="text-center">D</div>
          </div>

          <div className="grid grid-cols-7 gap-1">
            {cells.map((day, idx) => {
              if (!day) {
                return <div key={idx} className="h-9" />;
              }

              const isSelected =
                selectedDay === day &&
                selectedMonth === viewMonth &&
                selectedYear === viewYear;

              return (
                <button
                  key={idx}
                  type="button"
                  onClick={() => handlePick(day)}
                  className={
                    "h-9 rounded-lg text-sm hover:bg-blue-50 " +
                    (isSelected ? "bg-blue-600 text-white hover:bg-blue-700" : "text-gray-800")
                  }
                >
                  {day}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default NewClientPage;
