export function createSSEConnection(conversationId, message, onMessage, onDone) {
  const token = localStorage.getItem('token')
  const params = new URLSearchParams({ message })
  if (conversationId) params.append('conversationId', conversationId)

  const ctrl = new AbortController()
  fetch(`http://localhost:8080/api/chat/stream?${params}`, {
    headers: { 'Authorization': `Bearer ${token}` },
    signal: ctrl.signal
  }).then(async response => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    while (true) {
      const { done, value } = await reader.read()
      if (done) { onDone(); break }
      const text = decoder.decode(value, { stream: true })
      const lines = text.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          onMessage(line.slice(5))
        }
      }
    }
  }).catch(err => {
    if (err.name !== 'AbortError') console.error('SSE error:', err)
    onDone()
  })

  return { abort: () => ctrl.abort() }
}
