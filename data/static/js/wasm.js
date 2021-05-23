const worker = new Worker('/js/worker.js')
const workerThen = { nextId: 1 }
worker.onmessage = out => {
  const data = out.data
  if (out && data.id && workerThen[data.id]) {
    workerThen[data.id](data)
    delete workerThen[data.id]
  }
}

const execute = data => {
  const workerId = workerThen.nextId++
  data.id = workerId
  const promise = new Promise(resolve => {
    workerThen[workerId] = out => {
      resolve(out)
    }
  })
  worker.postMessage(data)
  return promise
}
export { execute }
