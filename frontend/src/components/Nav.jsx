const TABS = [
  { id: 'customers', label: 'Clientes' },
  { id: 'transfer', label: 'Transferencia' },
  { id: 'history', label: 'Histórico' },
];

export default function Nav({ current, onChange }) {
  return (
    <nav className="app-nav" aria-label="Navegación principal">
      {TABS.map((tab) => (
        <button
          key={tab.id}
          type="button"
          className={`app-nav__item ${current === tab.id ? 'app-nav__item--active' : ''}`}
          onClick={() => onChange(tab.id)}
          aria-current={current === tab.id ? 'page' : undefined}
        >
          {tab.label}
        </button>
      ))}
    </nav>
  );
}
