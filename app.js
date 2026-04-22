/* =============================================
   Expense Tracker — Application Logic
   ============================================= */

// =============================================
// Categories Definition
// =============================================
const EXPENSE_CATEGORIES = [
    { id: 'food', name: 'Food', icon: 'fa-utensils', color: '#f59e0b' },
    { id: 'transport', name: 'Transport', icon: 'fa-car', color: '#3b82f6' },
    { id: 'shopping', name: 'Shopping', icon: 'fa-bag-shopping', color: '#ec4899' },
    { id: 'entertainment', name: 'Entertainment', icon: 'fa-film', color: '#8b5cf6' },
    { id: 'bills', name: 'Bills', icon: 'fa-file-invoice-dollar', color: '#ef4444' },
    { id: 'health', name: 'Health', icon: 'fa-heart-pulse', color: '#10b981' },
    { id: 'education', name: 'Education', icon: 'fa-graduation-cap', color: '#06b6d4' },
    { id: 'personal', name: 'Personal', icon: 'fa-user', color: '#6366f1' },
    { id: 'other', name: 'Other', icon: 'fa-ellipsis', color: '#64748b' },
];

const INCOME_CATEGORIES = [
    { id: 'salary', name: 'Salary', icon: 'fa-briefcase', color: '#10b981' },
    { id: 'freelance', name: 'Freelance', icon: 'fa-laptop-code', color: '#06b6d4' },
    { id: 'investment', name: 'Investment', icon: 'fa-chart-line', color: '#8b5cf6' },
    { id: 'gift', name: 'Gift', icon: 'fa-gift', color: '#ec4899' },
    { id: 'other_income', name: 'Other', icon: 'fa-ellipsis', color: '#64748b' },
];

const CURRENCY_SYMBOLS = {
    USD: '$', EUR: '€', GBP: '£', JPY: '¥', INR: '₹'
};

// =============================================
// State
// =============================================
let state = {
    transactions: [],
    currency: 'USD',
    theme: 'light',
    currentView: 'home',
    currentPeriod: 'month',
    editingId: null,
    deletingId: null,
};

// =============================================
// Persistence (localStorage)
// =============================================
function loadState() {
    try {
        const data = localStorage.getItem('expenseTracker');
        if (data) {
            const parsed = JSON.parse(data);
            state.transactions = parsed.transactions || [];
            state.currency = parsed.currency || 'USD';
            state.theme = parsed.theme || 'light';
        }
    } catch (e) {
        console.error('Failed to load state:', e);
    }
}

function saveState() {
    try {
        localStorage.setItem('expenseTracker', JSON.stringify({
            transactions: state.transactions,
            currency: state.currency,
            theme: state.theme,
        }));
    } catch (e) {
        console.error('Failed to save state:', e);
    }
}

// =============================================
// Utility Functions
// =============================================
function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).slice(2, 9);
}

function formatCurrency(amount) {
    const sym = CURRENCY_SYMBOLS[state.currency] || '$';
    const abs = Math.abs(amount);
    const formatted = abs.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    return `${sym}${formatted}`;
}

function formatCurrencyWithSign(amount) {
    const sign = amount >= 0 ? '+' : '-';
    return `${sign}${formatCurrency(Math.abs(amount))}`;
}

function formatDate(dateStr) {
    const date = new Date(dateStr + 'T00:00:00');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const d = new Date(dateStr + 'T00:00:00');
    d.setHours(0, 0, 0, 0);

    if (d.getTime() === today.getTime()) return 'Today';
    if (d.getTime() === yesterday.getTime()) return 'Yesterday';
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateShort(dateStr) {
    const date = new Date(dateStr + 'T00:00:00');
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function getTodayStr() {
    const now = new Date();
    return now.toISOString().slice(0, 10);
}

function getCategory(id) {
    return EXPENSE_CATEGORIES.find(c => c.id === id)
        || INCOME_CATEGORIES.find(c => c.id === id)
        || { id: 'other', name: 'Other', icon: 'fa-ellipsis', color: '#64748b' };
}

function getCategoryColor(id) {
    return getCategory(id).color;
}

function getCategoryLightColor(color) {
    return color + '1a'; // ~10% opacity hex
}

function getPeriodStart() {
    const now = new Date();
    switch (state.currentPeriod) {
        case 'week':
            const w = new Date(now);
            w.setDate(w.getDate() - 7);
            return w.toISOString().slice(0, 10);
        case 'month':
            const m = new Date(now);
            m.setMonth(m.getMonth() - 1);
            return m.toISOString().slice(0, 10);
        case 'year':
            const y = new Date(now);
            y.setFullYear(y.getFullYear() - 1);
            return y.toISOString().slice(0, 10);
        case 'all':
            return '2000-01-01';
    }
}

function getTransactionsInPeriod() {
    const start = getPeriodStart();
    const end = getTodayStr();
    return state.transactions.filter(t => t.date >= start && t.date <= end);
}

// =============================================
// DOM Helpers
// =============================================
const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

function showToast(message) {
    let toast = document.querySelector('.toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2500);
}

// =============================================
// Navigation
// =============================================
function switchView(viewName) {
    state.currentView = viewName;

    $$('.view').forEach(v => v.classList.remove('active'));
    const target = $(`#${viewName}View`);
    if (target) target.classList.add('active');

    $$('.nav-btn').forEach(b => b.classList.remove('active'));
    const navBtn = $(`.nav-btn[data-view="${viewName}"]`);
    if (navBtn) navBtn.classList.add('active');

    // Scroll to top
    $('.main-content').scrollTop = 0;

    // Refresh
    if (viewName === 'home') refreshHome();
    if (viewName === 'transactions') refreshTransactions();
    if (viewName === 'reports') refreshReports();
}

// =============================================
// Home View
// =============================================
function refreshHome() {
    const totalIncome = state.transactions.filter(t => t.type === 'income').reduce((s, t) => s + t.amount, 0);
    const totalExpense = state.transactions.filter(t => t.type === 'expense').reduce((s, t) => s + t.amount, 0);
    const balance = totalIncome - totalExpense;

    $('#totalBalance').textContent = formatCurrency(balance);
    $('#totalIncome').textContent = formatCurrency(totalIncome);
    $('#totalExpense').textContent = formatCurrency(totalExpense);

    const now = new Date();
    $('#balanceDate').textContent = now.toLocaleDateString('en-US', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });

    // Recent Transactions (last 5)
    const recent = [...state.transactions]
        .sort((a, b) => b.date.localeCompare(a.date) || b.createdAt - a.createdAt)
        .slice(0, 5);

    const container = $('#recentTransactions');
    if (recent.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-receipt"></i>
                <p>No transactions yet</p>
                <span>Tap + to add your first transaction</span>
            </div>`;
    } else {
        container.innerHTML = recent.map(t => createTransactionItemHTML(t)).join('');
    }

    // Category Breakdown (last month expenses)
    const oneMonthAgo = new Date();
    oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
    const startStr = oneMonthAgo.toISOString().slice(0, 10);
    const expenses = state.transactions.filter(t =>
        t.type === 'expense' && t.date >= startStr && t.date <= getTodayStr()
    );

    const catContainer = $('#categoryBreakdown');
    if (expenses.length === 0) {
        catContainer.innerHTML = '<div class="empty-state sm"><p>No expense data yet</p></div>';
    } else {
        const totals = {};
        let total = 0;
        expenses.forEach(t => {
            totals[t.category] = (totals[t.category] || 0) + t.amount;
            total += t.amount;
        });

        const sorted = Object.entries(totals).sort((a, b) => b[1] - a[1]).slice(0, 5);
        catContainer.innerHTML = sorted.map(([catId, amount]) => {
            const cat = getCategory(catId);
            const pct = total > 0 ? (amount / total * 100).toFixed(1) : 0;
            return createCategoryItemHTML(cat, amount, pct);
        }).join('');
    }
}

function createTransactionItemHTML(t) {
    const cat = getCategory(t.category);
    const amountClass = t.type === 'income' ? 'income' : 'expense';
    const sign = t.type === 'income' ? '+' : '-';
    return `
        <div class="transaction-item" data-id="${t.id}" onclick="handleTransactionClick('${t.id}')">
            <div class="txn-icon" style="background: ${getCategoryLightColor(cat.color)}; color: ${cat.color}">
                <i class="fas ${cat.icon}"></i>
            </div>
            <div class="txn-details">
                <div class="txn-desc">${escapeHtml(t.description)}</div>
                <div class="txn-category" style="color: ${cat.color}">${cat.name}</div>
                <div class="txn-date">${formatDate(t.date)}${t.notes ? ' • 📝' : ''}</div>
            </div>
            <div class="txn-amount ${amountClass}">${sign}${formatCurrency(t.amount)}</div>
        </div>`;
}

function createCategoryItemHTML(cat, amount, pct) {
    return `
        <div class="category-item">
            <div class="cat-icon" style="background: ${getCategoryLightColor(cat.color)}; color: ${cat.color}">
                <i class="fas ${cat.icon}"></i>
            </div>
            <div class="cat-info">
                <div class="cat-top-row">
                    <span class="cat-name">${cat.name}</span>
                    <span class="cat-amount">${formatCurrency(amount)}</span>
                </div>
                <div class="cat-progress">
                    <div class="cat-progress-bar" style="width: ${pct}%; background: ${cat.color}"></div>
                </div>
            </div>
        </div>`;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// =============================================
// Transactions View
// =============================================
function refreshTransactions() {
    const searchTerm = ($('#searchInput')?.value || '').trim().toLowerCase();
    const typeFilter = $('#typeFilter')?.value || 'all';

    let filtered = [...state.transactions];

    if (typeFilter !== 'all') {
        filtered = filtered.filter(t => t.type === typeFilter);
    }

    if (searchTerm) {
        filtered = filtered.filter(t =>
            t.description.toLowerCase().includes(searchTerm)
            || (t.notes && t.notes.toLowerCase().includes(searchTerm))
            || getCategory(t.category).name.toLowerCase().includes(searchTerm)
        );
    }

    // Sort by date desc
    filtered.sort((a, b) => b.date.localeCompare(a.date) || b.createdAt - a.createdAt);

    const container = $('#allTransactions');
    if (filtered.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-receipt"></i>
                <p>No transactions found</p>
            </div>`;
        return;
    }

    // Group by date
    const grouped = {};
    filtered.forEach(t => {
        const key = t.date;
        if (!grouped[key]) grouped[key] = [];
        grouped[key].push(t);
    });

    let html = '';
    Object.entries(grouped).forEach(([date, txns]) => {
        html += `<div class="date-header">${formatDate(date)}</div>`;
        txns.forEach(t => {
            html += createTransactionItemHTML(t);
        });
    });

    container.innerHTML = html;
}

// =============================================
// Reports View
// =============================================
function refreshReports() {
    const transactions = getTransactionsInPeriod();
    const income = transactions.filter(t => t.type === 'income').reduce((s, t) => s + t.amount, 0);
    const expense = transactions.filter(t => t.type === 'expense').reduce((s, t) => s + t.amount, 0);
    const net = income - expense;

    $('#statIncome').textContent = formatCurrency(income);
    $('#statExpense').textContent = formatCurrency(expense);
    $('#statNet').textContent = formatCurrency(Math.abs(net));
    $('#statNet').style.color = net >= 0
        ? 'var(--success)' : 'var(--danger)';

    // Donut Chart
    drawDonutChart(transactions);

    // Category Breakdown
    drawCategoryBreakdown(transactions);

    // Bar Chart
    drawBarChart();
}

function drawDonutChart(transactions) {
    const canvas = $('#donutChart');
    const ctx = canvas.getContext('2d');
    const size = 220;
    canvas.width = size * 2;
    canvas.height = size * 2;
    canvas.style.width = size + 'px';
    canvas.style.height = size + 'px';
    ctx.scale(2, 2);
    ctx.clearRect(0, 0, size, size);

    const expenses = transactions.filter(t => t.type === 'expense');
    const totals = {};
    let total = 0;
    expenses.forEach(t => {
        totals[t.category] = (totals[t.category] || 0) + t.amount;
        total += t.amount;
    });

    $('#chartTotalAmount').textContent = formatCurrency(total);

    const legendContainer = $('#chartLegend');

    if (total === 0) {
        // Draw empty circle
        ctx.beginPath();
        ctx.arc(size / 2, size / 2, 75, 0, Math.PI * 2);
        ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--border').trim();
        ctx.lineWidth = 30;
        ctx.stroke();
        legendContainer.innerHTML = '<div class="legend-item" style="color:var(--text-muted)">No data</div>';
        return;
    }

    const entries = Object.entries(totals).sort((a, b) => b[1] - a[1]);
    const cx = size / 2;
    const cy = size / 2;
    const outerR = 82;
    const innerR = 52;
    let startAngle = -Math.PI / 2;

    entries.forEach(([catId, amount]) => {
        const cat = getCategory(catId);
        const sliceAngle = (amount / total) * Math.PI * 2;

        ctx.beginPath();
        ctx.moveTo(cx + innerR * Math.cos(startAngle), cy + innerR * Math.sin(startAngle));
        ctx.arc(cx, cy, outerR, startAngle, startAngle + sliceAngle);
        ctx.arc(cx, cy, innerR, startAngle + sliceAngle, startAngle, true);
        ctx.closePath();
        ctx.fillStyle = cat.color;
        ctx.fill();

        startAngle += sliceAngle;
    });

    // Legend
    legendContainer.innerHTML = entries.map(([catId]) => {
        const cat = getCategory(catId);
        return `<div class="legend-item">
            <span class="legend-dot" style="background:${cat.color}"></span>${cat.name}
        </div>`;
    }).join('');
}

function drawCategoryBreakdown(transactions) {
    const expenses = transactions.filter(t => t.type === 'expense');
    const totals = {};
    let total = 0;
    expenses.forEach(t => {
        totals[t.category] = (totals[t.category] || 0) + t.amount;
        total += t.amount;
    });

    const container = $('#reportCategoryBreakdown');
    if (total === 0) {
        container.innerHTML = '<div class="empty-state sm"><p>No expense data for this period</p></div>';
        return;
    }

    const sorted = Object.entries(totals).sort((a, b) => b[1] - a[1]);
    container.innerHTML = sorted.map(([catId, amount]) => {
        const cat = getCategory(catId);
        const pct = (amount / total * 100).toFixed(1);
        return createCategoryItemHTML(cat, amount, pct);
    }).join('');
}

function drawBarChart() {
    const canvas = $('#barChart');
    const ctx = canvas.getContext('2d');
    const w = canvas.parentElement.offsetWidth;
    const h = 200;
    canvas.width = w * 2;
    canvas.height = h * 2;
    canvas.style.width = w + 'px';
    canvas.style.height = h + 'px';
    ctx.scale(2, 2);
    ctx.clearRect(0, 0, w, h);

    const now = new Date();
    const months = [];
    for (let i = 5; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
        const start = d.toISOString().slice(0, 10);
        const end = new Date(d.getFullYear(), d.getMonth() + 1, 0).toISOString().slice(0, 10);
        const label = d.toLocaleDateString('en-US', { month: 'short' });
        const total = state.transactions
            .filter(t => t.type === 'expense' && t.date >= start && t.date <= end)
            .reduce((s, t) => s + t.amount, 0);
        months.push({ label, total });
    }

    const max = Math.max(...months.map(m => m.total), 1);
    const barWidth = Math.min(40, (w - 80) / 6 - 12);
    const gap = (w - 40 - months.length * barWidth) / (months.length + 1);
    const chartH = h - 40;

    const textColor = getComputedStyle(document.documentElement).getPropertyValue('--text-secondary').trim();
    const primaryColor = getComputedStyle(document.documentElement).getPropertyValue('--primary').trim() || '#6366f1';

    months.forEach((m, i) => {
        const x = 20 + gap + i * (barWidth + gap);
        const barH = max > 0 ? (m.total / max) * (chartH - 20) : 0;
        const y = chartH - barH;

        // Gradient bar
        const gradient = ctx.createLinearGradient(x, y, x, chartH);
        gradient.addColorStop(0, primaryColor);
        gradient.addColorStop(1, primaryColor + '60');
        ctx.fillStyle = gradient;
        ctx.beginPath();
        const r = Math.min(4, barWidth / 4);
        ctx.moveTo(x + r, y);
        ctx.lineTo(x + barWidth - r, y);
        ctx.quadraticCurveTo(x + barWidth, y, x + barWidth, y + r);
        ctx.lineTo(x + barWidth, chartH);
        ctx.lineTo(x, chartH);
        ctx.lineTo(x, y + r);
        ctx.quadraticCurveTo(x, y, x + r, y);
        ctx.closePath();
        ctx.fill();

        // Label
        ctx.fillStyle = textColor;
        ctx.font = '500 11px Inter';
        ctx.textAlign = 'center';
        ctx.fillText(m.label, x + barWidth / 2, h - 6);

        // Value
        if (m.total > 0) {
            ctx.fillStyle = getComputedStyle(document.documentElement).getPropertyValue('--text-primary').trim();
            ctx.font = '700 10px Inter';
            const valStr = m.total >= 1000 ? (m.total / 1000).toFixed(1) + 'k' : m.total.toFixed(0);
            ctx.fillText(valStr, x + barWidth / 2, y - 6);
        }
    });
}

// =============================================
// Transaction CRUD
// =============================================
function addTransaction(data) {
    const txn = {
        id: generateId(),
        type: data.type,
        amount: parseFloat(data.amount),
        description: data.description,
        category: data.category,
        date: data.date,
        notes: data.notes || '',
        createdAt: Date.now(),
    };
    state.transactions.unshift(txn);
    saveState();
    refreshAll();
    showToast('Transaction added ✓');
}

function updateTransaction(id, data) {
    const idx = state.transactions.findIndex(t => t.id === id);
    if (idx >= 0) {
        state.transactions[idx] = {
            ...state.transactions[idx],
            ...data,
            amount: parseFloat(data.amount),
        };
        saveState();
        refreshAll();
        showToast('Transaction updated ✓');
    }
}

function deleteTransaction(id) {
    state.transactions = state.transactions.filter(t => t.id !== id);
    saveState();
    refreshAll();
    showToast('Transaction deleted');
}

function refreshAll() {
    if (state.currentView === 'home') refreshHome();
    if (state.currentView === 'transactions') refreshTransactions();
    if (state.currentView === 'reports') refreshReports();
}

// =============================================
// Modal Handling
// =============================================
function openAddModal(editId = null) {
    state.editingId = editId;
    const modal = $('#addModal');
    modal.classList.add('active');

    if (editId) {
        const txn = state.transactions.find(t => t.id === editId);
        if (txn) {
            $('#modalTitle').textContent = 'Edit Transaction';
            selectType(txn.type);
            $('#amountInput').value = txn.amount;
            $('#descInput').value = txn.description;
            $('#dateInput').value = txn.date;
            $('#notesInput').value = txn.notes || '';
            renderCategoryGrid(txn.type === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES, txn.category);
        }
    } else {
        $('#modalTitle').textContent = 'Add Transaction';
        selectType('expense');
        $('#amountInput').value = '';
        $('#descInput').value = '';
        $('#dateInput').value = getTodayStr();
        $('#notesInput').value = '';
        renderCategoryGrid(EXPENSE_CATEGORIES);
    }

    updateModalCurrencySymbol();

    // Focus amount after animation
    setTimeout(() => $('#amountInput').focus(), 350);
}

function closeAddModal() {
    const modal = $('#addModal');
    modal.classList.remove('active');
    state.editingId = null;
}

function openDeleteModal(id) {
    state.deletingId = id;
    $('#deleteModal').classList.add('active');
}

function closeDeleteModal() {
    state.deletingId = null;
    $('#deleteModal').classList.remove('active');
}

function selectType(type) {
    $$('.type-btn').forEach(b => b.classList.remove('active'));
    $(`.type-btn[data-type="${type}"]`).classList.add('active');

    const cats = type === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
    renderCategoryGrid(cats);
}

function renderCategoryGrid(categories, selectedId = null) {
    const grid = $('#categoryGrid');
    grid.innerHTML = categories.map((cat, i) => {
        const sel = selectedId ? cat.id === selectedId : i === 0;
        return `
            <button type="button" class="cat-btn ${sel ? 'selected' : ''}"
                data-cat="${cat.id}"
                style="${sel ? `border-color: ${cat.color}; background: ${getCategoryLightColor(cat.color)}` : ''}"
                onclick="selectCategory(this, '${cat.color}')">
                <i class="fas ${cat.icon}" style="color: ${cat.color}"></i>
                ${cat.name}
            </button>`;
    }).join('');
}

function selectCategory(btn, color) {
    $$('.cat-btn').forEach(b => {
        b.classList.remove('selected');
        b.style.borderColor = '';
        b.style.background = '';
    });
    btn.classList.add('selected');
    btn.style.borderColor = color;
    btn.style.background = getCategoryLightColor(color);
}

function updateModalCurrencySymbol() {
    $('#modalCurrencySymbol').textContent = CURRENCY_SYMBOLS[state.currency] || '$';
}

function handleSave() {
    const amount = parseFloat($('#amountInput').value);
    const description = $('#descInput').value.trim();
    const date = $('#dateInput').value;
    const notes = $('#notesInput').value.trim();

    const activeType = $('.type-btn.active');
    const type = activeType ? activeType.dataset.type : 'expense';

    const selectedCat = $('.cat-btn.selected');
    const category = selectedCat ? selectedCat.dataset.cat : 'other';

    // Validation
    if (!amount || amount <= 0) {
        showToast('Please enter a valid amount');
        $('#amountInput').focus();
        return;
    }
    if (!description) {
        showToast('Please enter a description');
        $('#descInput').focus();
        return;
    }
    if (!date) {
        showToast('Please select a date');
        return;
    }

    const data = { type, amount, description, category, date, notes };

    if (state.editingId) {
        updateTransaction(state.editingId, data);
    } else {
        addTransaction(data);
    }

    closeAddModal();
}

function handleTransactionClick(id) {
    openAddModal(id);
}

// =============================================
// Theme
// =============================================
function applyTheme() {
    document.documentElement.setAttribute('data-theme', state.theme);
    $('#darkModeToggle').checked = state.theme === 'dark';
}

function toggleTheme() {
    state.theme = state.theme === 'dark' ? 'light' : 'dark';
    applyTheme();
    saveState();

    // Re-render charts if on reports
    if (state.currentView === 'reports') {
        setTimeout(refreshReports, 100);
    }
}

// =============================================
// Settings
// =============================================
function exportData() {
    const data = JSON.stringify(state.transactions, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `expense-tracker-backup-${getTodayStr()}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showToast('Data exported successfully ✓');
}

function clearAllData() {
    state.transactions = [];
    saveState();
    refreshAll();
    closeDeleteModal();
    showToast('All data cleared');
}

// =============================================
// Initialize & Event Listeners
// =============================================
function init() {
    loadState();
    applyTheme();
    refreshHome();

    // Date input default
    $('#dateInput').value = getTodayStr();

    // Currency select
    $('#currencySelect').value = state.currency;

    // Set balance date
    const now = new Date();
    $('#balanceDate').textContent = now.toLocaleDateString('en-US', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });

    // --- Navigation ---
    $$('.nav-btn[data-view]').forEach(btn => {
        btn.addEventListener('click', () => switchView(btn.dataset.view));
    });

    $('#navAdd').addEventListener('click', () => openAddModal());
    $('#headerSettingsBtn').addEventListener('click', () => switchView('settings'));
    $('#settingsBackBtn').addEventListener('click', () => switchView('home'));
    $('#viewAllTransactions').addEventListener('click', () => switchView('transactions'));

    // --- Modal ---
    $('#modalCloseBtn').addEventListener('click', closeAddModal);
    $('#modalCancelBtn').addEventListener('click', closeAddModal);
    $('#modalSaveBtn').addEventListener('click', handleSave);
    $('#addModal').addEventListener('click', (e) => {
        if (e.target === $('#addModal')) closeAddModal();
    });

    // --- Type Toggle ---
    $$('.type-btn').forEach(btn => {
        btn.addEventListener('click', () => selectType(btn.dataset.type));
    });

    // --- Delete Modal ---
    $('#deleteCancelBtn').addEventListener('click', closeDeleteModal);
    $('#deleteConfirmBtn').addEventListener('click', () => {
        if (state.deletingId) {
            deleteTransaction(state.deletingId);
            closeDeleteModal();
        }
    });
    $('#deleteModal').addEventListener('click', (e) => {
        if (e.target === $('#deleteModal')) closeDeleteModal();
    });

    // --- Search & Filter ---
    $('#searchInput').addEventListener('input', () => refreshTransactions());
    $('#typeFilter').addEventListener('change', () => refreshTransactions());

    // --- Period Selector ---
    $$('.period-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            $$('.period-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            state.currentPeriod = btn.dataset.period;
            refreshReports();
        });
    });

    // --- Settings ---
    $('#darkModeToggle').addEventListener('change', toggleTheme);

    $('#currencySelect').addEventListener('change', () => {
        state.currency = $('#currencySelect').value;
        saveState();
        refreshAll();
        updateModalCurrencySymbol();
        showToast(`Currency changed to ${state.currency}`);
    });

    $('#exportDataBtn').addEventListener('click', exportData);

    $('#clearDataBtn').addEventListener('click', () => {
        if (confirm('Are you sure you want to delete ALL transaction data?\n\nThis action cannot be undone!')) {
            clearAllData();
        }
    });

    // Keyboard: Enter to save in modal
    $('#addModalContent').addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
            e.preventDefault();
            handleSave();
        }
    });

    // Render initial category grid
    renderCategoryGrid(EXPENSE_CATEGORIES);

    // Window resize for bar chart
    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            if (state.currentView === 'reports') drawBarChart();
        }, 200);
    });
}

document.addEventListener('DOMContentLoaded', init);
