<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import {
  Building2,
  CalendarClock,
  CheckCircle2,
  CircleDollarSign,
  ClipboardList,
  Eye,
  Mail,
  MessageSquarePlus,
  Pencil,
  Phone,
  Plus,
  RefreshCcw,
  Save,
  Search,
  Target,
  Trash2,
  UserRound,
  X
} from 'lucide-vue-next';

const views = [
  { key: 'customers', label: '客户', title: '客户管理', eyebrow: '客户资产', icon: ClipboardList },
  { key: 'followups', label: '跟进', title: '跟进计划', eyebrow: '销售动作', icon: CalendarClock },
  { key: 'opportunities', label: '商机', title: '商机管道', eyebrow: '收入预测', icon: CircleDollarSign }
];

const stageOptions = [
  { value: 'NEW', label: '新客户', tone: 'tone-slate' },
  { value: 'CONTACTED', label: '已联系', tone: 'tone-amber' },
  { value: 'QUALIFIED', label: '已确认', tone: 'tone-blue' },
  { value: 'NEGOTIATION', label: '谈判中', tone: 'tone-violet' },
  { value: 'WON', label: '已成交', tone: 'tone-green' },
  { value: 'LOST', label: '已流失', tone: 'tone-red' }
];

const opportunityStageOptions = [
  { value: 'DISCOVERY', label: '发现需求', probability: 20, tone: 'tone-slate' },
  { value: 'PROPOSAL', label: '方案报价', probability: 45, tone: 'tone-blue' },
  { value: 'NEGOTIATION', label: '商务谈判', probability: 70, tone: 'tone-violet' },
  { value: 'CONTRACT', label: '合同确认', probability: 85, tone: 'tone-amber' },
  { value: 'WON', label: '赢单', probability: 100, tone: 'tone-green' },
  { value: 'LOST', label: '输单', probability: 0, tone: 'tone-red' }
];

const followTypeOptions = ['电话', '微信', '会议', '邮件', '拜访'];

const activeView = ref('customers');
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');

const dashboard = ref(null);
const opportunitySummary = ref(null);
const customers = ref([]);
const followUps = ref([]);
const opportunities = ref([]);
const selectedCustomer = ref(null);
const selectedOpportunity = ref(null);

const customerFilters = reactive({
  keyword: '',
  stage: ''
});

const followFilters = reactive({
  keyword: '',
  dueOnly: false
});

const opportunityFilters = reactive({
  keyword: '',
  stage: ''
});

const showCustomerDialog = ref(false);
const showFollowDialog = ref(false);
const showOpportunityDialog = ref(false);
const editingCustomerId = ref(null);
const editingOpportunityId = ref(null);

const blankContact = (primaryContact = false) => ({
  name: '',
  title: '',
  phone: '',
  email: '',
  primaryContact
});

const blankCustomer = () => ({
  name: '',
  industry: '',
  source: '官网咨询',
  ownerName: '',
  stage: 'NEW',
  dealValue: 0,
  phone: '',
  email: '',
  address: '',
  remark: '',
  nextFollowDate: '',
  contacts: [blankContact(true)]
});

const blankFollow = (customer = null) => ({
  customerId: customer?.id ?? '',
  ownerName: customer?.ownerName ?? '',
  type: '电话',
  content: '',
  followDate: today(),
  nextFollowDate: customer?.nextFollowDate ?? ''
});

const blankOpportunity = (customer = null) => ({
  customerId: customer?.id ?? '',
  name: '',
  stage: 'DISCOVERY',
  amount: Number(customer?.dealValue ?? 0),
  probability: 20,
  ownerName: customer?.ownerName ?? '',
  source: customer?.source ?? '',
  expectedCloseDate: '',
  nextStep: '',
  remark: ''
});

const customerForm = reactive(blankCustomer());
const followForm = reactive(blankFollow());
const opportunityForm = reactive(blankOpportunity());

const viewMeta = computed(() => views.find((view) => view.key === activeView.value) ?? views[0]);

const primaryActionLabel = computed(() => {
  if (activeView.value === 'followups') {
    return '新增跟进';
  }
  if (activeView.value === 'opportunities') {
    return '新建商机';
  }
  return '新建客户';
});

const selectedPrimaryContact = computed(() => {
  const contacts = selectedCustomer.value?.contacts ?? [];
  return contacts.find((contact) => contact.primaryContact) ?? contacts[0] ?? null;
});

const selectedCustomerStage = computed(() => getCustomerStage(selectedCustomer.value?.stage));
const selectedOpportunityStage = computed(() => getOpportunityStage(selectedOpportunity.value?.stage));

const dueFollowUps = computed(() => followUps.value.filter((followUp) => isDue(followUp.nextFollowDate)));
const weekFollowUps = computed(() => {
  const now = new Date(`${today()}T00:00:00`);
  const nextWeek = new Date(now);
  nextWeek.setDate(now.getDate() + 7);
  return followUps.value.filter((followUp) => {
    if (!followUp.followDate) {
      return false;
    }
    const date = new Date(`${followUp.followDate}T00:00:00`);
    return date >= now && date <= nextWeek;
  });
});

const opportunitiesByStage = computed(() => {
  const grouped = new Map(opportunityStageOptions.map((stage) => [stage.value, []]));
  for (const opportunity of opportunities.value) {
    grouped.get(opportunity.stage)?.push(opportunity);
  }
  return grouped;
});

async function request(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {})
    },
    ...options
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `请求失败：${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

async function loadDashboard() {
  dashboard.value = await request('/dashboard');
}

async function loadCustomers({ keepSelection = false } = {}) {
  const params = new URLSearchParams();
  if (customerFilters.keyword.trim()) {
    params.set('keyword', customerFilters.keyword.trim());
  }
  if (customerFilters.stage) {
    params.set('stage', customerFilters.stage);
  }

  customers.value = await request(`/customers${params.toString() ? `?${params}` : ''}`);

  const selectedId = selectedCustomer.value?.id;
  const nextCustomer = keepSelection && selectedId
    ? customers.value.find((customer) => customer.id === selectedId)
    : customers.value[0];
  selectedCustomer.value = nextCustomer ? await request(`/customers/${nextCustomer.id}`) : null;
}

async function loadFollowUps() {
  const params = new URLSearchParams();
  if (followFilters.keyword.trim()) {
    params.set('keyword', followFilters.keyword.trim());
  }
  if (followFilters.dueOnly) {
    params.set('dueOnly', 'true');
  }
  followUps.value = await request(`/follow-ups${params.toString() ? `?${params}` : ''}`);
}

async function loadOpportunities({ keepSelection = false } = {}) {
  const params = new URLSearchParams();
  if (opportunityFilters.keyword.trim()) {
    params.set('keyword', opportunityFilters.keyword.trim());
  }
  if (opportunityFilters.stage) {
    params.set('stage', opportunityFilters.stage);
  }

  opportunities.value = await request(`/opportunities${params.toString() ? `?${params}` : ''}`);

  const selectedId = selectedOpportunity.value?.id;
  selectedOpportunity.value =
    keepSelection && selectedId
      ? opportunities.value.find((opportunity) => opportunity.id === selectedId) ?? opportunities.value[0] ?? null
      : opportunities.value[0] ?? null;
}

async function loadOpportunitySummary() {
  opportunitySummary.value = await request('/opportunities/summary');
}

async function loadAll(options = {}) {
  loading.value = true;
  errorMessage.value = '';
  try {
    await Promise.all([
      loadDashboard(),
      loadCustomers({ keepSelection: options.keepCustomer }),
      loadFollowUps(),
      loadOpportunities({ keepSelection: options.keepOpportunity }),
      loadOpportunitySummary()
    ]);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

function switchView(view) {
  activeView.value = view;
}

function runPrimaryAction() {
  if (activeView.value === 'followups') {
    openFollowDialog();
  } else if (activeView.value === 'opportunities') {
    openOpportunityDialog();
  } else {
    openCreateCustomerDialog();
  }
}

async function refreshCurrent() {
  await loadAll({ keepCustomer: true, keepOpportunity: true });
}

async function selectCustomer(customer) {
  errorMessage.value = '';
  selectedCustomer.value = await request(`/customers/${customer.id}`);
}

async function openCustomerFromReference(customerId) {
  activeView.value = 'customers';
  selectedCustomer.value = await request(`/customers/${customerId}`);
}

function resetCustomerForm(customer = null) {
  const next = customer
    ? {
        name: customer.name,
        industry: customer.industry,
        source: customer.source,
        ownerName: customer.ownerName,
        stage: customer.stage,
        dealValue: Number(customer.dealValue ?? 0),
        phone: customer.phone ?? '',
        email: customer.email ?? '',
        address: customer.address ?? '',
        remark: customer.remark ?? '',
        nextFollowDate: customer.nextFollowDate ?? '',
        contacts: customer.contacts?.length
          ? customer.contacts.map((contact) => ({ ...contact }))
          : [blankContact(true)]
      }
    : blankCustomer();

  Object.assign(customerForm, next);
}

function openCreateCustomerDialog() {
  editingCustomerId.value = null;
  resetCustomerForm();
  showCustomerDialog.value = true;
}

function openEditCustomerDialog() {
  if (!selectedCustomer.value) {
    return;
  }
  editingCustomerId.value = selectedCustomer.value.id;
  resetCustomerForm(selectedCustomer.value);
  showCustomerDialog.value = true;
}

function addContact() {
  customerForm.contacts.push(blankContact(customerForm.contacts.length === 0));
}

function removeContact(index) {
  customerForm.contacts.splice(index, 1);
  if (customerForm.contacts.length && !customerForm.contacts.some((contact) => contact.primaryContact)) {
    customerForm.contacts[0].primaryContact = true;
  }
}

function setPrimaryContact(index) {
  customerForm.contacts.forEach((contact, contactIndex) => {
    contact.primaryContact = contactIndex === index;
  });
}

async function saveCustomer() {
  saving.value = true;
  errorMessage.value = '';
  try {
    const payload = {
      ...customerForm,
      dealValue: Number(customerForm.dealValue || 0),
      nextFollowDate: customerForm.nextFollowDate || null,
      contacts: customerForm.contacts
        .filter((contact) => contact.name || contact.title || contact.phone || contact.email)
        .map((contact, index) => ({
          name: contact.name,
          title: contact.title,
          phone: contact.phone,
          email: contact.email,
          primaryContact: contact.primaryContact || index === 0
        }))
    };

    const saved = editingCustomerId.value
      ? await request(`/customers/${editingCustomerId.value}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        })
      : await request('/customers', {
          method: 'POST',
          body: JSON.stringify(payload)
        });

    selectedCustomer.value = saved;
    showCustomerDialog.value = false;
    await loadAll({ keepCustomer: true, keepOpportunity: true });
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    saving.value = false;
  }
}

async function deleteCustomer() {
  if (!selectedCustomer.value) {
    return;
  }
  if (!window.confirm(`确认删除 ${selectedCustomer.value.name}？`)) {
    return;
  }

  await request(`/customers/${selectedCustomer.value.id}`, { method: 'DELETE' });
  selectedCustomer.value = null;
  await loadAll({ keepOpportunity: true });
}

function openFollowDialog(customer = null) {
  Object.assign(followForm, blankFollow(customer ?? selectedCustomer.value));
  showFollowDialog.value = true;
}

async function saveFollowUp() {
  saving.value = true;
  errorMessage.value = '';
  try {
    await request('/follow-ups', {
      method: 'POST',
      body: JSON.stringify({
        ...followForm,
        customerId: Number(followForm.customerId),
        followDate: followForm.followDate || null,
        nextFollowDate: followForm.nextFollowDate || null
      })
    });
    showFollowDialog.value = false;
    await loadAll({ keepCustomer: true, keepOpportunity: true });
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    saving.value = false;
  }
}

function resetOpportunityForm(opportunity = null, customer = null) {
  const next = opportunity
    ? {
        customerId: opportunity.customerId,
        name: opportunity.name,
        stage: opportunity.stage,
        amount: Number(opportunity.amount ?? 0),
        probability: Number(opportunity.probability ?? 20),
        ownerName: opportunity.ownerName ?? '',
        source: opportunity.source ?? '',
        expectedCloseDate: opportunity.expectedCloseDate ?? '',
        nextStep: opportunity.nextStep ?? '',
        remark: opportunity.remark ?? ''
      }
    : blankOpportunity(customer);

  Object.assign(opportunityForm, next);
}

function openOpportunityDialog(opportunity = null, customer = null) {
  editingOpportunityId.value = opportunity?.id ?? null;
  resetOpportunityForm(opportunity, customer ?? selectedCustomer.value);
  showOpportunityDialog.value = true;
}

function selectOpportunity(opportunity) {
  selectedOpportunity.value = opportunity;
}

async function saveOpportunity() {
  saving.value = true;
  errorMessage.value = '';
  try {
    const payload = {
      ...opportunityForm,
      customerId: Number(opportunityForm.customerId),
      amount: Number(opportunityForm.amount || 0),
      probability: Number(opportunityForm.probability || 0),
      expectedCloseDate: opportunityForm.expectedCloseDate || null
    };

    selectedOpportunity.value = editingOpportunityId.value
      ? await request(`/opportunities/${editingOpportunityId.value}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        })
      : await request('/opportunities', {
          method: 'POST',
          body: JSON.stringify(payload)
        });

    showOpportunityDialog.value = false;
    await loadAll({ keepCustomer: true, keepOpportunity: true });
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    saving.value = false;
  }
}

async function deleteOpportunity(opportunity = selectedOpportunity.value) {
  if (!opportunity) {
    return;
  }
  if (!window.confirm(`确认删除商机 ${opportunity.name}？`)) {
    return;
  }

  await request(`/opportunities/${opportunity.id}`, { method: 'DELETE' });
  selectedOpportunity.value = null;
  await loadAll({ keepCustomer: true });
}

function getCustomerStage(value) {
  return stageOptions.find((stage) => stage.value === value) ?? stageOptions[0];
}

function getOpportunityStage(value) {
  return opportunityStageOptions.find((stage) => stage.value === value) ?? opportunityStageOptions[0];
}

function formatCurrency(value) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    maximumFractionDigits: 0
  }).format(Number(value ?? 0));
}

function formatDate(value) {
  if (!value) {
    return '未设置';
  }
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(`${value}T00:00:00`));
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

function isDue(value) {
  return Boolean(value && value <= today());
}

function stageTotal(stageValue) {
  return (opportunitiesByStage.value.get(stageValue) ?? []).reduce(
    (sum, opportunity) => sum + Number(opportunity.amount ?? 0),
    0
  );
}

onMounted(() => loadAll());
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">CRM</div>
        <div>
          <strong>销售工作台</strong>
          <span>客户 · 跟进 · 商机</span>
        </div>
      </div>

      <nav class="nav-stack">
        <button
          v-for="view in views"
          :key="view.key"
          class="nav-item"
          :class="{ active: activeView === view.key }"
          type="button"
          @click="switchView(view.key)"
        >
          <component :is="view.icon" :size="18" />
          {{ view.label }}
        </button>
      </nav>

      <div class="sidebar-stat">
        <span>今日待办</span>
        <strong>{{ dashboard?.followUpsDue ?? 0 }}</strong>
        <small>进行中客户 {{ dashboard?.openCustomers ?? 0 }}</small>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">{{ viewMeta.eyebrow }}</p>
          <h1>{{ viewMeta.title }}</h1>
        </div>
        <div class="topbar-actions">
          <button class="icon-button" type="button" title="刷新" aria-label="刷新" @click="refreshCurrent">
            <RefreshCcw :size="18" />
          </button>
          <button class="primary-button" type="button" @click="runPrimaryAction">
            <Plus :size="18" />
            {{ primaryActionLabel }}
          </button>
        </div>
      </header>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <template v-if="activeView === 'customers'">
        <section class="metrics">
          <article class="metric-card">
            <span>客户总数</span>
            <strong>{{ dashboard?.totalCustomers ?? 0 }}</strong>
          </article>
          <article class="metric-card">
            <span>进行中客户</span>
            <strong>{{ dashboard?.openCustomers ?? 0 }}</strong>
          </article>
          <article class="metric-card">
            <span>预计金额</span>
            <strong>{{ formatCurrency(dashboard?.openDealValue) }}</strong>
          </article>
          <article class="metric-card">
            <span>成交金额</span>
            <strong>{{ formatCurrency(dashboard?.wonDealValue) }}</strong>
          </article>
        </section>

        <section class="content-grid">
          <section class="work-panel">
            <div class="panel-toolbar">
              <label class="search-field">
                <Search :size="18" />
                <input
                  v-model="customerFilters.keyword"
                  type="search"
                  placeholder="客户、行业、负责人"
                  @keyup.enter="loadCustomers({ keepSelection: true })"
                />
              </label>
              <select v-model="customerFilters.stage" @change="loadCustomers()">
                <option value="">全部阶段</option>
                <option v-for="stage in stageOptions" :key="stage.value" :value="stage.value">
                  {{ stage.label }}
                </option>
              </select>
              <button class="secondary-button" type="button" @click="loadCustomers({ keepSelection: true })">
                <Search :size="16" />
                查询
              </button>
            </div>

            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>客户</th>
                    <th>阶段</th>
                    <th>负责人</th>
                    <th>金额</th>
                    <th>下次跟进</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="customer in customers"
                    :key="customer.id"
                    :class="{ selected: selectedCustomer?.id === customer.id }"
                    @click="selectCustomer(customer)"
                  >
                    <td>
                      <strong>{{ customer.name }}</strong>
                      <span>{{ customer.industry }} · {{ customer.primaryContactName || '暂无联系人' }}</span>
                    </td>
                    <td>
                      <span class="status-pill" :class="getCustomerStage(customer.stage).tone">
                        {{ getCustomerStage(customer.stage).label }}
                      </span>
                    </td>
                    <td>{{ customer.ownerName }}</td>
                    <td>{{ formatCurrency(customer.dealValue) }}</td>
                    <td>
                      <span :class="{ due: isDue(customer.nextFollowDate) }">
                        {{ formatDate(customer.nextFollowDate) }}
                      </span>
                    </td>
                    <td>
                      <button class="row-button" type="button" title="查看" aria-label="查看" @click.stop="selectCustomer(customer)">
                        <Eye :size="16" />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-if="!customers.length && !loading" class="empty-state">暂无客户</div>
            </div>
          </section>

          <aside class="detail-panel" :class="{ empty: !selectedCustomer }">
            <template v-if="selectedCustomer">
              <div class="detail-header">
                <div>
                  <span class="status-pill" :class="selectedCustomerStage.tone">{{ selectedCustomerStage.label }}</span>
                  <h2>{{ selectedCustomer.name }}</h2>
                  <p>{{ selectedCustomer.industry }} · {{ selectedCustomer.source }}</p>
                </div>
                <div class="detail-actions">
                  <button class="icon-button" type="button" title="编辑" aria-label="编辑" @click="openEditCustomerDialog">
                    <Pencil :size="17" />
                  </button>
                  <button class="icon-button danger" type="button" title="删除" aria-label="删除" @click="deleteCustomer">
                    <Trash2 :size="17" />
                  </button>
                </div>
              </div>

              <div class="quick-actions">
                <button class="secondary-button compact" type="button" @click="openFollowDialog(selectedCustomer)">
                  <MessageSquarePlus :size="16" />
                  跟进
                </button>
                <button class="secondary-button compact" type="button" @click="openOpportunityDialog(null, selectedCustomer)">
                  <Target :size="16" />
                  商机
                </button>
              </div>

              <div class="detail-facts">
                <div>
                  <Building2 :size="17" />
                  <span>{{ selectedCustomer.address || '地址未设置' }}</span>
                </div>
                <div>
                  <UserRound :size="17" />
                  <span>{{ selectedCustomer.ownerName }}</span>
                </div>
                <div>
                  <Phone :size="17" />
                  <span>{{ selectedCustomer.phone || selectedPrimaryContact?.phone || '电话未设置' }}</span>
                </div>
                <div>
                  <Mail :size="17" />
                  <span>{{ selectedCustomer.email || selectedPrimaryContact?.email || '邮箱未设置' }}</span>
                </div>
              </div>

              <div class="detail-amount">
                <span>预计金额</span>
                <strong>{{ formatCurrency(selectedCustomer.dealValue) }}</strong>
              </div>

              <div class="detail-section">
                <div class="section-title">
                  <h3>联系人</h3>
                </div>
                <ul class="contact-list">
                  <li v-for="contact in selectedCustomer.contacts" :key="contact.id">
                    <div>
                      <strong>{{ contact.name }}</strong>
                      <span>{{ contact.title || '未设置职务' }}</span>
                    </div>
                    <div>
                      <span>{{ contact.phone || '未设置电话' }}</span>
                      <span>{{ contact.email || '未设置邮箱' }}</span>
                    </div>
                  </li>
                </ul>
              </div>

              <div class="detail-section">
                <div class="section-title">
                  <h3>跟进记录</h3>
                </div>
                <ol class="timeline">
                  <li v-for="follow in selectedCustomer.followUps" :key="follow.id">
                    <time>{{ formatDate(follow.followDate) }}</time>
                    <div>
                      <strong>{{ follow.type || '跟进' }} · {{ follow.ownerName || selectedCustomer.ownerName }}</strong>
                      <p>{{ follow.content }}</p>
                      <span>下次：{{ formatDate(follow.nextFollowDate) }}</span>
                    </div>
                  </li>
                </ol>
                <div v-if="!selectedCustomer.followUps.length" class="empty-state small">暂无跟进记录</div>
              </div>
            </template>

            <div v-else class="detail-placeholder">
              <ClipboardList :size="38" />
              <span>暂无选中客户</span>
            </div>
          </aside>
        </section>
      </template>

      <template v-else-if="activeView === 'followups'">
        <section class="metrics">
          <article class="metric-card">
            <span>待处理</span>
            <strong>{{ dueFollowUps.length }}</strong>
          </article>
          <article class="metric-card">
            <span>本周跟进</span>
            <strong>{{ weekFollowUps.length }}</strong>
          </article>
          <article class="metric-card">
            <span>记录总数</span>
            <strong>{{ followUps.length }}</strong>
          </article>
          <article class="metric-card">
            <span>跟进客户</span>
            <strong>{{ new Set(followUps.map((follow) => follow.customerId)).size }}</strong>
          </article>
        </section>

        <section class="content-grid follow-grid">
          <section class="work-panel">
            <div class="panel-toolbar">
              <label class="search-field">
                <Search :size="18" />
                <input
                  v-model="followFilters.keyword"
                  type="search"
                  placeholder="客户、内容、负责人"
                  @keyup.enter="loadFollowUps"
                />
              </label>
              <label class="toggle-control">
                <input v-model="followFilters.dueOnly" type="checkbox" @change="loadFollowUps" />
                只看待处理
              </label>
              <button class="secondary-button" type="button" @click="loadFollowUps">
                <Search :size="16" />
                查询
              </button>
            </div>

            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>客户</th>
                    <th>内容</th>
                    <th>方式</th>
                    <th>负责人</th>
                    <th>跟进日期</th>
                    <th>下次</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="follow in followUps" :key="follow.id">
                    <td>
                      <strong>{{ follow.customerName }}</strong>
                      <span>{{ getCustomerStage(follow.customerStage).label }}</span>
                    </td>
                    <td class="wide-cell">{{ follow.content }}</td>
                    <td>{{ follow.type || '跟进' }}</td>
                    <td>{{ follow.ownerName || '未设置' }}</td>
                    <td>{{ formatDate(follow.followDate) }}</td>
                    <td>
                      <span :class="{ due: isDue(follow.nextFollowDate) }">{{ formatDate(follow.nextFollowDate) }}</span>
                    </td>
                    <td>
                      <button
                        class="row-button"
                        type="button"
                        title="客户详情"
                        aria-label="客户详情"
                        @click="openCustomerFromReference(follow.customerId)"
                      >
                        <Eye :size="16" />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-if="!followUps.length && !loading" class="empty-state">暂无跟进记录</div>
            </div>
          </section>

          <aside class="side-panel">
            <div class="section-title">
              <h3>今日优先</h3>
              <span class="count-badge">{{ dueFollowUps.length }}</span>
            </div>
            <ul class="task-list">
              <li v-for="follow in dueFollowUps" :key="follow.id">
                <div>
                  <strong>{{ follow.customerName }}</strong>
                  <span>{{ follow.ownerName || '未设置负责人' }} · {{ formatDate(follow.nextFollowDate) }}</span>
                </div>
                <p>{{ follow.content }}</p>
                <button class="link-button" type="button" @click="openFollowDialog({ id: follow.customerId, ownerName: follow.ownerName })">
                  补记录
                </button>
              </li>
            </ul>
            <div v-if="!dueFollowUps.length" class="empty-state small">暂无待处理跟进</div>
          </aside>
        </section>
      </template>

      <template v-else>
        <section class="metrics">
          <article class="metric-card">
            <span>打开商机</span>
            <strong>{{ opportunitySummary?.openOpportunities ?? 0 }}</strong>
          </article>
          <article class="metric-card">
            <span>管道金额</span>
            <strong>{{ formatCurrency(opportunitySummary?.openAmount) }}</strong>
          </article>
          <article class="metric-card">
            <span>加权预测</span>
            <strong>{{ formatCurrency(opportunitySummary?.weightedAmount) }}</strong>
          </article>
          <article class="metric-card">
            <span>赢单金额</span>
            <strong>{{ formatCurrency(opportunitySummary?.wonAmount) }}</strong>
          </article>
        </section>

        <section class="work-panel">
          <div class="panel-toolbar">
            <label class="search-field">
              <Search :size="18" />
              <input
                v-model="opportunityFilters.keyword"
                type="search"
                placeholder="商机、客户、负责人"
                @keyup.enter="loadOpportunities({ keepSelection: true })"
              />
            </label>
            <select v-model="opportunityFilters.stage" @change="loadOpportunities()">
              <option value="">全部阶段</option>
              <option v-for="stage in opportunityStageOptions" :key="stage.value" :value="stage.value">
                {{ stage.label }}
              </option>
            </select>
            <button class="secondary-button" type="button" @click="loadOpportunities({ keepSelection: true })">
              <Search :size="16" />
              查询
            </button>
          </div>

          <div class="pipeline">
            <section v-for="stage in opportunityStageOptions" :key="stage.value" class="pipeline-column">
              <header>
                <span class="status-pill" :class="stage.tone">{{ stage.label }}</span>
                <strong>{{ formatCurrency(stageTotal(stage.value)) }}</strong>
              </header>
              <button
                v-for="opportunity in opportunitiesByStage.get(stage.value)"
                :key="opportunity.id"
                class="pipeline-item"
                :class="{ selected: selectedOpportunity?.id === opportunity.id }"
                type="button"
                @click="selectOpportunity(opportunity)"
              >
                <strong>{{ opportunity.name }}</strong>
                <span>{{ opportunity.customerName }}</span>
                <small>{{ formatCurrency(opportunity.amount) }} · {{ opportunity.probability }}%</small>
              </button>
              <div v-if="!opportunitiesByStage.get(stage.value)?.length" class="pipeline-empty">无</div>
            </section>
          </div>
        </section>

        <section class="content-grid opportunity-grid">
          <section class="work-panel">
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>商机</th>
                    <th>客户</th>
                    <th>阶段</th>
                    <th>金额</th>
                    <th>概率</th>
                    <th>预计签约</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="opportunity in opportunities"
                    :key="opportunity.id"
                    :class="{ selected: selectedOpportunity?.id === opportunity.id }"
                    @click="selectOpportunity(opportunity)"
                  >
                    <td>
                      <strong>{{ opportunity.name }}</strong>
                      <span>{{ opportunity.ownerName }}</span>
                    </td>
                    <td>{{ opportunity.customerName }}</td>
                    <td>
                      <span class="status-pill" :class="getOpportunityStage(opportunity.stage).tone">
                        {{ getOpportunityStage(opportunity.stage).label }}
                      </span>
                    </td>
                    <td>{{ formatCurrency(opportunity.amount) }}</td>
                    <td>{{ opportunity.probability }}%</td>
                    <td>{{ formatDate(opportunity.expectedCloseDate) }}</td>
                    <td>
                      <button
                        class="row-button"
                        type="button"
                        title="编辑"
                        aria-label="编辑"
                        @click.stop="openOpportunityDialog(opportunity)"
                      >
                        <Pencil :size="16" />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-if="!opportunities.length && !loading" class="empty-state">暂无商机</div>
            </div>
          </section>

          <aside class="detail-panel" :class="{ empty: !selectedOpportunity }">
            <template v-if="selectedOpportunity">
              <div class="detail-header">
                <div>
                  <span class="status-pill" :class="selectedOpportunityStage.tone">{{ selectedOpportunityStage.label }}</span>
                  <h2>{{ selectedOpportunity.name }}</h2>
                  <p>{{ selectedOpportunity.customerName }} · {{ selectedOpportunity.ownerName }}</p>
                </div>
                <div class="detail-actions">
                  <button class="icon-button" type="button" title="编辑" aria-label="编辑" @click="openOpportunityDialog(selectedOpportunity)">
                    <Pencil :size="17" />
                  </button>
                  <button class="icon-button danger" type="button" title="删除" aria-label="删除" @click="deleteOpportunity()">
                    <Trash2 :size="17" />
                  </button>
                </div>
              </div>

              <div class="detail-amount">
                <span>商机金额</span>
                <strong>{{ formatCurrency(selectedOpportunity.amount) }}</strong>
              </div>

              <div class="detail-facts">
                <div>
                  <Target :size="17" />
                  <span>加权 {{ formatCurrency(selectedOpportunity.weightedAmount) }} · {{ selectedOpportunity.probability }}%</span>
                </div>
                <div>
                  <CalendarClock :size="17" />
                  <span>{{ formatDate(selectedOpportunity.expectedCloseDate) }}</span>
                </div>
                <div>
                  <Building2 :size="17" />
                  <span>{{ selectedOpportunity.source || '来源未设置' }}</span>
                </div>
                <div>
                  <CheckCircle2 :size="17" />
                  <span>{{ selectedOpportunity.nextStep || '下一步未设置' }}</span>
                </div>
              </div>

              <div class="detail-section">
                <div class="section-title">
                  <h3>备注</h3>
                </div>
                <p class="note-text">{{ selectedOpportunity.remark || '暂无备注' }}</p>
              </div>
            </template>

            <div v-else class="detail-placeholder">
              <CircleDollarSign :size="38" />
              <span>暂无选中商机</span>
            </div>
          </aside>
        </section>
      </template>
    </main>

    <div v-if="showCustomerDialog" class="modal-backdrop">
      <form class="modal-card" @submit.prevent="saveCustomer">
        <div class="modal-header">
          <h2>{{ editingCustomerId ? '编辑客户' : '新建客户' }}</h2>
          <button class="icon-button" type="button" title="关闭" aria-label="关闭" @click="showCustomerDialog = false">
            <X :size="18" />
          </button>
        </div>

        <div class="form-grid">
          <label>
            客户名称
            <input v-model.trim="customerForm.name" required />
          </label>
          <label>
            所属行业
            <input v-model.trim="customerForm.industry" required />
          </label>
          <label>
            来源
            <input v-model.trim="customerForm.source" required />
          </label>
          <label>
            负责人
            <input v-model.trim="customerForm.ownerName" required />
          </label>
          <label>
            阶段
            <select v-model="customerForm.stage">
              <option v-for="stage in stageOptions" :key="stage.value" :value="stage.value">
                {{ stage.label }}
              </option>
            </select>
          </label>
          <label>
            预计金额
            <input v-model.number="customerForm.dealValue" min="0" type="number" />
          </label>
          <label>
            电话
            <input v-model.trim="customerForm.phone" />
          </label>
          <label>
            邮箱
            <input v-model.trim="customerForm.email" type="email" />
          </label>
          <label class="wide">
            地址
            <input v-model.trim="customerForm.address" />
          </label>
          <label>
            下次跟进
            <input v-model="customerForm.nextFollowDate" type="date" />
          </label>
          <label class="wide">
            备注
            <textarea v-model.trim="customerForm.remark" rows="3"></textarea>
          </label>
        </div>

        <div class="contact-editor">
          <div class="section-title">
            <h3>联系人</h3>
            <button class="secondary-button compact" type="button" @click="addContact">
              <Plus :size="16" />
              添加
            </button>
          </div>
          <div v-for="(contact, index) in customerForm.contacts" :key="index" class="contact-editor-row">
            <input v-model.trim="contact.name" placeholder="姓名" />
            <input v-model.trim="contact.title" placeholder="职务" />
            <input v-model.trim="contact.phone" placeholder="电话" />
            <input v-model.trim="contact.email" type="email" placeholder="邮箱" />
            <button
              class="chip-button"
              type="button"
              :class="{ active: contact.primaryContact }"
              @click="setPrimaryContact(index)"
            >
              主要
            </button>
            <button class="row-button danger" type="button" title="移除" aria-label="移除" @click="removeContact(index)">
              <X :size="15" />
            </button>
          </div>
        </div>

        <div class="modal-footer">
          <button class="secondary-button" type="button" @click="showCustomerDialog = false">取消</button>
          <button class="primary-button" type="submit" :disabled="saving">
            <Save :size="17" />
            保存
          </button>
        </div>
      </form>
    </div>

    <div v-if="showFollowDialog" class="modal-backdrop">
      <form class="modal-card narrow" @submit.prevent="saveFollowUp">
        <div class="modal-header">
          <h2>新增跟进</h2>
          <button class="icon-button" type="button" title="关闭" aria-label="关闭" @click="showFollowDialog = false">
            <X :size="18" />
          </button>
        </div>

        <div class="form-grid single">
          <label>
            客户
            <select v-model.number="followForm.customerId" required>
              <option value="">选择客户</option>
              <option v-for="customer in customers" :key="customer.id" :value="customer.id">
                {{ customer.name }}
              </option>
            </select>
          </label>
          <label>
            负责人
            <input v-model.trim="followForm.ownerName" />
          </label>
          <label>
            类型
            <select v-model="followForm.type">
              <option v-for="type in followTypeOptions" :key="type">{{ type }}</option>
            </select>
          </label>
          <label>
            跟进日期
            <input v-model="followForm.followDate" type="date" />
          </label>
          <label>
            下次跟进
            <input v-model="followForm.nextFollowDate" type="date" />
          </label>
          <label class="wide">
            内容
            <textarea v-model.trim="followForm.content" required rows="4"></textarea>
          </label>
        </div>

        <div class="modal-footer">
          <button class="secondary-button" type="button" @click="showFollowDialog = false">取消</button>
          <button class="primary-button" type="submit" :disabled="saving">
            <Save :size="17" />
            保存
          </button>
        </div>
      </form>
    </div>

    <div v-if="showOpportunityDialog" class="modal-backdrop">
      <form class="modal-card" @submit.prevent="saveOpportunity">
        <div class="modal-header">
          <h2>{{ editingOpportunityId ? '编辑商机' : '新建商机' }}</h2>
          <button class="icon-button" type="button" title="关闭" aria-label="关闭" @click="showOpportunityDialog = false">
            <X :size="18" />
          </button>
        </div>

        <div class="form-grid">
          <label>
            客户
            <select v-model.number="opportunityForm.customerId" required>
              <option value="">选择客户</option>
              <option v-for="customer in customers" :key="customer.id" :value="customer.id">
                {{ customer.name }}
              </option>
            </select>
          </label>
          <label>
            商机名称
            <input v-model.trim="opportunityForm.name" required />
          </label>
          <label>
            阶段
            <select v-model="opportunityForm.stage">
              <option v-for="stage in opportunityStageOptions" :key="stage.value" :value="stage.value">
                {{ stage.label }}
              </option>
            </select>
          </label>
          <label>
            负责人
            <input v-model.trim="opportunityForm.ownerName" required />
          </label>
          <label>
            金额
            <input v-model.number="opportunityForm.amount" min="0" type="number" />
          </label>
          <label>
            预计签约
            <input v-model="opportunityForm.expectedCloseDate" type="date" />
          </label>
          <label>
            来源
            <input v-model.trim="opportunityForm.source" />
          </label>
          <label>
            概率 {{ opportunityForm.probability }}%
            <input v-model.number="opportunityForm.probability" max="100" min="0" step="5" type="range" />
          </label>
          <label class="wide">
            下一步
            <textarea v-model.trim="opportunityForm.nextStep" rows="3"></textarea>
          </label>
          <label class="wide">
            备注
            <textarea v-model.trim="opportunityForm.remark" rows="3"></textarea>
          </label>
        </div>

        <div class="modal-footer">
          <button class="secondary-button" type="button" @click="showOpportunityDialog = false">取消</button>
          <button class="primary-button" type="submit" :disabled="saving">
            <Save :size="17" />
            保存
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
