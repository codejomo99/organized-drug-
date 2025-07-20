const logBox      = document.getElementById("log");
const processedEl = document.getElementById("processed");
const totalEl     = document.getElementById("total");
totalEl.textContent = '10000';

const socket = new WebSocket("ws://localhost:8080/ws/logs");
socket.onmessage = event => {
    try {
        const obj = JSON.parse(event.data);
        if (obj.type === 'progress') {
            processedEl.textContent = obj.processed;
            totalEl.textContent     = obj.total;
            return;
        }
    } catch (_) {

    }
    logBox.textContent += event.data + "\n";
    logBox.scrollTop = logBox.scrollHeight;
};

function startOrganize() {
    fetch('/organize', { method: 'POST' });
}

function stopOrganize() {
    fetch('/organize/stop', { method: 'POST' });
}

function setOrganize() {
    logBox.textContent += "[IMPORT] 진행중...\n";
    logBox.scrollTop = logBox.scrollHeight;
    fetch('/organize/import', { method: 'POST' })
        .then(res => res.text())
        .then(text => {
            logBox.textContent = logBox.textContent.replace(
                /\[IMPORT\] 진행중\.\.\.\n$/,
                `[IMPORT] 완료: ${text}\n`
            );
            logBox.scrollTop = logBox.scrollHeight;
        })
        .catch(err => {
            logBox.textContent = logBox.textContent.replace(
                /\[IMPORT\] 진행중\.\.\.\n$/,
                `[IMPORT] 오류: ${err}\n`
            );
            logBox.scrollTop = logBox.scrollHeight;
        });
}
