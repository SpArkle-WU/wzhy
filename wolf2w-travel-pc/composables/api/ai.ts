import request from '@/utils/request'

export interface AiSource {
  chunkId: string
  sourceType: string
  sourceId: number
  chunkNo: number
  title: string
}

export interface AiAnswer {
  answer: string
  sources: AiSource[]
  knowledgeBased: boolean
}

export function askTravelAssistant(question: string, topK = 3): Promise<{ data: AiAnswer }> {
  return request.post('/ai/ask', { question, topK })
}
