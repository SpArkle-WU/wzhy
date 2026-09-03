<template>
  <main class="ai-page">
    <div class="ai-shell">
      <aside class="conversation-sidebar">
        <div class="assistant-mark">
          <div class="mark-icon">
            <el-icon :size="24"><MagicStick /></el-icon>
          </div>
          <div>
            <h1>旅行 AI</h1>
            <p>攻略问答</p>
          </div>
        </div>

        <button class="new-question" type="button" @click="startNewQuestion">
          <el-icon><EditPen /></el-icon>
          新问题
        </button>

        <section class="history-section">
          <h2>最近提问</h2>
          <button v-for="item in history" :key="item.id" class="history-item" :class="{ active: selectedHistoryId === item.id }" type="button" @click="restoreHistory(item)">
            <span>{{ item.question }}</span>
          </button>
          <p v-if="history.length === 0" class="history-empty">暂无记录</p>
        </section>
      </aside>

      <section class="answer-workspace">
        <header class="workspace-header">
          <div>
            <p class="eyebrow">TRAVEL KNOWLEDGE BASE</p>
            <h2>问问你的下一次旅行</h2>
          </div>
          <span class="status-dot"><i></i> 攻略库在线</span>
        </header>

        <div class="dialogue-area" :class="{ 'has-answer': currentAnswer }">
          <template v-if="currentAnswer">
            <div class="question-bubble">
              <span>你</span>
              <p>{{ askedQuestion }}</p>
            </div>

            <div class="answer-block">
              <div class="answer-avatar">
                <el-icon><MagicStick /></el-icon>
              </div>
              <div class="answer-content">
                <p class="answer-label">
                  {{ currentAnswer.knowledgeBased ? '知识库回答' : '通用 AI 回答' }}
                </p>
                <!-- formattedAnswer escapes all model text before applying limited presentation markup. -->
                <!-- eslint-disable-next-line vue/no-v-html -->
                <div class="answer-text" v-html="formattedAnswer"></div>

                <section v-if="currentAnswer.knowledgeBased && currentAnswer.sources.length" class="source-section">
                  <h3>参考攻略</h3>
                  <div class="source-list">
                    <NuxtLink v-for="source in currentAnswer.sources" :key="source.chunkId" class="source-item" :to="sourceLink(source)">
                      <span class="source-icon">
                        <el-icon><Document /></el-icon>
                      </span>
                      <span class="source-copy">
                        <strong>{{ source.title || source.chunkId }}</strong>
                        <small>攻略 ID {{ source.sourceId }}</small>
                      </span>
                      <el-icon class="source-arrow"><ArrowRight /></el-icon>
                    </NuxtLink>
                  </div>
                </section>
              </div>
            </div>
          </template>

          <div v-else-if="loading" class="loading-state">
            <el-icon class="is-loading" :size="26"><Loading /></el-icon>
            <p>正在检索攻略资料</p>
          </div>

          <div v-else class="empty-state">
            <div class="empty-icon">
              <el-icon :size="34"><ChatDotRound /></el-icon>
            </div>
            <h3>旅行问题，交给攻略库</h3>
            <div class="suggestion-list">
              <button v-for="suggestion in suggestions" :key="suggestion" type="button" class="suggestion" @click="askSuggestion(suggestion)">
                {{ suggestion }}
                <el-icon><ArrowUpRight /></el-icon>
              </button>
            </div>
          </div>
        </div>

        <p v-if="requestError" class="request-error">
          <el-icon><WarningFilled /></el-icon>
          {{ requestError }}
        </p>

        <form class="composer" @submit.prevent="submitQuestion">
          <textarea ref="composerInput" v-model="question" maxlength="300" rows="3" placeholder="例如：带孩子去广州玩两天，怎么安排？" aria-label="输入旅行问题" @keydown="onComposerKeydown"></textarea>
          <div class="composer-toolbar">
            <span class="composer-hint">按 Ctrl / Cmd + Enter 发送</span>
            <span class="composer-count">{{ question.length }} / 300</span>
            <button class="send-button" type="submit" :disabled="!question.trim() || loading">
              <el-icon v-if="loading" class="is-loading"><Loading /></el-icon>
              <el-icon v-else><Promotion /></el-icon>
              <span>{{ loading ? '思考中' : '发送' }}</span>
            </button>
          </div>
        </form>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { askTravelAssistant, type AiAnswer, type AiSource } from '@/composables/api/ai'

interface HistoryItem {
  id: number
  question: string
  answer: AiAnswer
}

const HISTORY_KEY = 'travel-ai-assistant-history'
const MAX_HISTORY_SIZE = 8

const suggestions = ['广州适合带孩子去哪里玩？', '去广州长隆野生动物园要注意什么？', '亲子游动物园怎样安排路线？']

const question = ref('')
const askedQuestion = ref('')
const currentAnswer = ref<AiAnswer | null>(null)
const history = ref<HistoryItem[]>([])
const selectedHistoryId = ref<number | null>(null)
const loading = ref(false)
const requestError = ref('')
const composerInput = ref<HTMLTextAreaElement | null>(null)

const formattedAnswer = computed(() => {
  if (!currentAnswer.value) {
    return ''
  }

  return escapeHtml(currentAnswer.value.answer)
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\[来源: ([^\]]+)\]/g, '<span class="citation">来源: $1</span>')
    .replace(/\n/g, '<br>')
})

onMounted(() => {
  const storedHistory = sessionStorage.getItem(HISTORY_KEY)
  if (!storedHistory) {
    return
  }

  try {
    const parsedHistory = JSON.parse(storedHistory)
    if (Array.isArray(parsedHistory)) {
      history.value = parsedHistory.slice(0, MAX_HISTORY_SIZE)
    }
  } catch {
    sessionStorage.removeItem(HISTORY_KEY)
  }
})

function escapeHtml(value: string) {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#039;')
}

function sourceLink(source: AiSource) {
  return source.sourceType === 'strategy' ? `/strategy/article/${source.sourceId}` : '/strategy'
}

function persistHistory() {
  sessionStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}

function addToHistory(questionText: string, answer: AiAnswer) {
  const item: HistoryItem = {
    id: Date.now(),
    question: questionText,
    answer
  }
  history.value = [item, ...history.value.filter(entry => entry.question !== questionText)].slice(0, MAX_HISTORY_SIZE)
  selectedHistoryId.value = item.id
  persistHistory()
}

async function submitQuestion() {
  const questionText = question.value.trim()
  if (!questionText || loading.value) {
    return
  }

  loading.value = true
  requestError.value = ''
  askedQuestion.value = questionText
  currentAnswer.value = null
  selectedHistoryId.value = null

  try {
    const response = await askTravelAssistant(questionText)
    currentAnswer.value = response.data
    addToHistory(questionText, response.data)
  } catch (error: any) {
    const message = error?.msg || error?.message
    requestError.value = message || '暂时无法获取回答，请稍后再试。'
  } finally {
    loading.value = false
  }
}

function askSuggestion(suggestion: string) {
  question.value = suggestion
  submitQuestion()
}

function restoreHistory(item: HistoryItem) {
  question.value = item.question
  askedQuestion.value = item.question
  currentAnswer.value = item.answer
  selectedHistoryId.value = item.id
  requestError.value = ''
}

function startNewQuestion() {
  question.value = ''
  askedQuestion.value = ''
  currentAnswer.value = null
  selectedHistoryId.value = null
  requestError.value = ''
  nextTick(() => composerInput.value?.focus())
}

function onComposerKeydown(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault()
    submitQuestion()
  }
}
</script>

<style lang="scss" scoped>
.ai-page,
.ai-page * {
  box-sizing: border-box;
}

.ai-page {
  min-height: calc(100dvh - 68px);
  padding: clamp(16px, 2.5vw, 32px);
  background: #f3f5f8;
  box-sizing: border-box;
  overflow-x: hidden;
}

.ai-shell {
  display: grid;
  grid-template-columns: minmax(220px, 250px) minmax(0, 1fr);
  width: 100%;
  max-width: 1320px;
  height: min(820px, calc(100dvh - 116px));
  min-height: 520px;
  margin: 0 auto;
  overflow: hidden;
  border: 1px solid #dfe4ea;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(29, 41, 57, 0.08);
}

.conversation-sidebar {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 28px 20px;
  border-right: 1px solid #e7ebef;
  background: #fbfcfd;
}

.assistant-mark {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 4px;

  .mark-icon {
    display: grid;
    width: 42px;
    height: 42px;
    place-items: center;
    border-radius: 8px;
    background: #192885;
    color: #fff;
  }

  h1 {
    margin: 0 0 5px;
    color: #18212f;
    font-size: 17px;
    font-weight: 700;
    line-height: 1;
  }

  p {
    color: #7a8491;
    font-size: 12px;
  }
}

.new-question {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  width: 100%;
  height: 42px;
  margin-top: 28px;
  border: 1px solid #cfd7e4;
  border-radius: 6px;
  background: #fff;
  color: #253c9c;
  font-size: 14px;
  font-family: inherit;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;

  &:hover {
    border-color: #253c9c;
    background: #f2f4ff;
  }
}

.history-section {
  min-height: 0;
  flex: 1;
  margin-top: 28px;
  overflow-y: auto;

  h2 {
    margin: 0 8px 10px;
    color: #7a8491;
    font-size: 12px;
  }
}

.history-item {
  width: 100%;
  margin-bottom: 4px;
  padding: 10px 9px;
  overflow: hidden;
  border: 0;
  border-radius: 5px;
  background: transparent;
  color: #4a5563;
  font-size: 13px;
  font-family: inherit;
  line-height: 18px;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;

  &:hover,
  &.active {
    background: #eaf0ff;
    color: #192885;
  }
}

.history-empty {
  margin: 18px 8px;
  color: #9da6b2;
  font-size: 13px;
}

.answer-workspace {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  padding: 32px clamp(28px, 4vw, 56px) 24px;
}

.workspace-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding-bottom: 22px;
  border-bottom: 1px solid #e8ecf1;

  .eyebrow {
    margin-bottom: 8px;
    color: #68805a;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0;
  }

  h2 {
    color: #18212f;
    font-size: clamp(22px, 2vw, 28px);
    font-weight: 700;
    line-height: 1.25;
  }
}

.status-dot {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  gap: 7px;
  padding-top: 8px;
  color: #71806a;
  font-size: 12px;

  i {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: #65a65c;
  }
}

.dialogue-area {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 0;
  padding: 26px 0 22px;
  overflow-y: auto;

  &.has-answer {
    justify-content: flex-start;
  }
}

.empty-state {
  width: min(640px, 100%);
  margin: auto;
  padding: 30px 0 20px;

  .empty-icon {
    display: grid;
    width: 58px;
    height: 58px;
    margin-bottom: 18px;
    place-items: center;
    border: 1px solid #d7dfef;
    border-radius: 8px;
    background: #eff3ff;
    color: #253c9c;
  }

  h3 {
    margin-bottom: 22px;
    color: #263342;
    font-size: 18px;
    font-weight: 700;
  }
}

.suggestion-list {
  display: grid;
  gap: 9px;
}

.suggestion {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 42px;
  padding: 10px 14px;
  border: 1px solid #e0e6ed;
  border-radius: 6px;
  background: #fff;
  color: #52606e;
  font-size: 14px;
  font-family: inherit;
  text-align: left;
  cursor: pointer;

  .el-icon {
    color: #8492a1;
  }

  &:hover {
    border-color: #8fa0dd;
    color: #192885;
  }
}

.loading-state {
  display: grid;
  place-items: center;
  gap: 14px;
  color: #64758a;
  font-size: 14px;
}

.question-bubble {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 10px;
  margin-left: auto;
  width: min(680px, 88%);
  max-width: 100%;

  span {
    display: grid;
    flex: 0 0 28px;
    width: 28px;
    height: 28px;
    place-items: center;
    border-radius: 50%;
    background: #edf0f4;
    color: #4c5a68;
    font-size: 12px;
  }

  p {
    padding: 11px 14px;
    border-radius: 7px 0 7px 7px;
    background: #253c9c;
    color: #fff;
    font-size: 14px;
    line-height: 1.55;
    overflow-wrap: anywhere;
  }
}

.answer-block {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  width: min(760px, 100%);
  margin-top: 30px;
}

.answer-avatar {
  display: grid;
  flex: 0 0 30px;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 7px;
  background: #edf4ea;
  color: #527844;
}

.answer-content {
  min-width: 0;
  flex: 1;
}

.answer-label {
  margin: 5px 0 10px;
  color: #536271;
  font-size: 13px;
  font-weight: 700;
}

.answer-text {
  color: #303d4b;
  font-size: 15px;
  line-height: 1.8;
  overflow-wrap: anywhere;

  :deep(strong) {
    color: #1f2c3a;
    font-weight: 700;
  }

  :deep(.citation) {
    display: inline-block;
    margin: 0 2px;
    padding: 1px 6px;
    border-radius: 4px;
    background: #fff3df;
    color: #9a6417;
    font-size: 12px;
    line-height: 1.5;
  }
}

.source-section {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #e8ecf1;

  h3 {
    margin-bottom: 10px;
    color: #6d7783;
    font-size: 13px;
    font-weight: 700;
  }
}

.source-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 10px;
}

.source-item {
  display: flex;
  align-items: center;
  min-width: 0;
  min-height: 58px;
  padding: 9px 10px;
  border: 1px solid #e1e6eb;
  border-radius: 6px;
  color: #354555;

  &:hover {
    border-color: #9babe2;
    background: #f7f9ff;
  }
}

.source-icon {
  display: grid;
  flex: 0 0 30px;
  width: 30px;
  height: 30px;
  margin-right: 9px;
  place-items: center;
  border-radius: 5px;
  background: #eef1f5;
  color: #52606e;
}

.source-copy {
  min-width: 0;
  flex: 1;

  strong,
  small {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: #334354;
    font-size: 13px;
    font-weight: 600;
  }

  small {
    margin-top: 5px;
    color: #8a96a2;
    font-size: 11px;
  }
}

.source-arrow {
  margin-left: 8px;
  color: #7f8ca0;
}

.request-error {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0 0 12px;
  padding: 10px 12px;
  border: 1px solid #efcccc;
  border-radius: 6px;
  background: #fff7f7;
  color: #a44848;
  font-size: 13px;
}

.composer {
  position: relative;
  flex: 0 0 auto;
  min-height: 112px;
  padding: 15px 16px 52px;
  box-sizing: border-box;
  border: 1px solid #cfd8e3;
  border-radius: 10px;
  background: #fff;

  &:focus-within {
    border-color: #6076cf;
    box-shadow: 0 0 0 3px rgba(37, 60, 156, 0.1);
  }

  textarea {
    display: block;
    width: 100%;
    min-height: 50px;
    max-height: 120px;
    padding: 3px 0;
    resize: none;
    outline: 0;
    border: 0;
    background: transparent;
    color: #344353;
    font-family: inherit;
    font-size: 15px;
    line-height: 1.6;
    box-sizing: border-box;

    &::placeholder {
      color: #9aa5b1;
    }
  }

  .composer-toolbar {
    position: absolute;
    right: 12px;
    bottom: 11px;
    left: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    min-height: 36px;
    color: #99a3af;
    font-size: 11px;
    line-height: 18px;
  }

  .composer-hint {
    color: #9aa5b1;
  }

  .composer-count {
    margin-left: auto;
    white-space: nowrap;
  }
}

.send-button {
  position: static;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 92px;
  height: 36px;
  flex: 0 0 92px;
  border: 0;
  border-radius: 6px;
  background: #253c9c;
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: #192b79;
  }

  &:disabled {
    background: #b8c2d6;
    cursor: not-allowed;
  }
}

@media (max-width: 980px) {
  .ai-page {
    padding: 16px;
  }

  .ai-shell {
    grid-template-columns: 208px minmax(0, 1fr);
    min-height: 480px;
  }

  .conversation-sidebar {
    padding: 20px 14px;
  }

  .answer-workspace {
    padding: 26px 28px 22px;
  }

  .workspace-header {
    padding-bottom: 18px;
  }
}

@media (max-height: 700px) and (min-width: 761px) {
  .ai-page {
    padding: 16px;
  }

  .ai-shell {
    min-height: 0;
    height: calc(100dvh - 84px);
  }

  .answer-workspace {
    padding: 20px 28px 18px;
  }

  .workspace-header {
    padding-bottom: 14px;

    h2 {
      font-size: 22px;
    }
  }

  .status-dot {
    padding-top: 5px;
  }

  .dialogue-area {
    justify-content: flex-start;
    padding: 16px 0;
  }

  .empty-state {
    width: 100%;

    .empty-icon {
      display: none;
    }

    h3 {
      margin-bottom: 12px;
      font-size: 16px;
    }
  }

  .suggestion-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .suggestion {
    min-height: 34px;
    padding: 8px 10px;
    font-size: 12px;

    &:last-child {
      grid-column: 1 / -1;
    }
  }

  .composer {
    min-height: 96px;
    padding-top: 10px;
    padding-bottom: 48px;

    textarea {
      min-height: 36px;
    }
  }
}

@media (max-width: 760px) {
  .ai-page {
    min-height: calc(100dvh - 68px);
    padding: 0;
  }

  .ai-shell {
    display: block;
    height: auto;
    min-height: calc(100dvh - 68px);
    max-height: none;
    border: 0;
    border-radius: 0;
    box-shadow: none;
  }

  .conversation-sidebar {
    display: block;
    padding: 14px 18px;
    border-right: 0;
    border-bottom: 1px solid #e7ebef;
  }

  .assistant-mark {
    display: inline-flex;
  }

  .new-question {
    float: right;
    width: 92px;
    margin-top: 2px;
  }

  .history-section {
    display: none;
  }

  .answer-workspace {
    min-height: calc(100dvh - 137px);
    padding: 22px 18px 18px;
  }

  .workspace-header h2 {
    font-size: 21px;
  }

  .status-dot {
    display: none;
  }

  .dialogue-area {
    min-height: 280px;
    padding-top: 18px;
  }

  .question-bubble {
    width: 94%;
  }

  .answer-block {
    margin-top: 22px;
  }

  .composer {
    min-height: 112px;
    padding-right: 12px;
  }

  .composer-hint {
    display: none;
  }

  .source-list {
    grid-template-columns: 1fr;
  }
}
</style>
