export function createSSEConnection(conversationId, message, onMessage, onDone, onMovies) {
  const token = localStorage.getItem('token')
  const params = new URLSearchParams({ message })
  if (conversationId) params.append('conversationId', conversationId)

  const ctrl = new AbortController()
  fetch(`http://localhost:8080/api/chat/stream?${params}`, {
    headers: { 'Authorization': `Bearer ${token}` },
    signal: ctrl.signal
  }).then(async response => {
    if (!response.ok) {
      console.error('SSE response error:', response.status)
      onDone()
      return
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    function parseBlock(block) {
      if (!block.trim()) return
      let eventName = null
      let dataLines = []

      for (const line of block.split('\n')) {
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5))
        }
      }

      if (!dataLines.length) return
      const data = dataLines.join('\n')

      if (eventName === 'movies' && onMovies) {
        try {
          let parsed = data
          if (parsed.startsWith('"') && parsed.endsWith('"')) {
            parsed = JSON.parse(parsed)
          }
          const movies = JSON.parse(parsed)
          onMovies(movies)
        } catch (e) {
          console.error('Failed to parse movies data:', e)
        }
      } else if (!eventName) {
        let text = data
        if (text.startsWith('"') && text.endsWith('"')) {
          try { text = JSON.parse(text) } catch (e) { /* keep as is */ }
        }
        onMessage(text)
      }
    }

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      const blocks = buffer.split('\n\n')
      buffer = blocks.pop() // keep incomplete last block
      for (const block of blocks) {
        parseBlock(block)
      }
    }

    // flush: process whatever remains in buffer after stream ends
    if (buffer.trim()) {
      parseBlock(buffer)
    }

    onDone()
  }).catch(err => {
    if (err.name !== 'AbortError') console.error('SSE error:', err)
    onDone()
  })

  return { abort: () => ctrl.abort() }
}
