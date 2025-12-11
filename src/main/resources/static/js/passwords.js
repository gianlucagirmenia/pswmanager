// passwords.js

// Funzione per mostrare/nascondere la password nella tabella
function toggleDisplayPassword(button) {
	const tableRow = button.closest('tr');
	const passwordDisplay = tableRow.querySelector('.password-display');
	const originalPassword = passwordDisplay.getAttribute('data-password');
	const currentText = passwordDisplay.textContent;

	// Se il testo attuale è la password originale, nascondila
	if (currentText === originalPassword ||
		currentText === originalPassword.substring(0, 20) + '...') {
		// Nascondi con asterischi
		const displayLength = Math.min(originalPassword.length, 20);
		passwordDisplay.textContent = '•'.repeat(displayLength);
		if (originalPassword.length > 20) {
			passwordDisplay.textContent += '...';
		}
		button.textContent = '👁️';
		button.title = 'Mostra password';
	} else {
		// Mostra la password originale
		if (originalPassword.length > 20) {
			passwordDisplay.textContent = originalPassword.substring(0, 20) + '...';
		} else {
			passwordDisplay.textContent = originalPassword;
		}
		button.textContent = '🙈';
		button.title = 'Nascondi password';
	}
}

// Funzione per copiare la password
function copyPassword(button, passwordText) {
	navigator.clipboard.writeText(passwordText).then(function() {
		const originalText = button.textContent;
		const originalTitle = button.title;
		const originalBackground = button.style.background;

		button.textContent = '✅';
		button.title = 'Copiato!';
		button.style.background = '#27ae60';

		setTimeout(function() {
			button.textContent = originalText;
			button.title = originalTitle;
			button.style.background = originalBackground;
		}, 1500);

	}).catch(function(err) {
		console.error('Errore nella copia: ', err);
		alert('Errore nella copia della password. Controlla i permessi del browser.');
	});
}

// Funzione per filtrare la tabella in base alla ricerca
function filterTable() {
	const searchTerm = document.getElementById('searchInput').value.toLowerCase();
	const table = document.getElementById('passwordTable');
	const noResults = document.getElementById('noResults');

	if (!table) return;

	const tbody = table.getElementsByTagName('tbody')[0];
	const rows = tbody.getElementsByTagName('tr');

	let found = false;

	for (let i = 0; i < rows.length; i++) {
		const titleCell = rows[i].getElementsByTagName('td')[0];
		const titleText = titleCell.textContent || titleCell.innerText;

		if (titleText.toLowerCase().includes(searchTerm)) {
			rows[i].style.display = '';
			found = true;
		} else {
			rows[i].style.display = 'none';
		}
	}

	if (!found && searchTerm !== '') {
		table.style.display = 'none';
		noResults.style.display = 'block';
	} else {
		table.style.display = '';
		noResults.style.display = 'none';
	}
}

// Funzione per pulire la ricerca
function clearSearch() {
	document.getElementById('searchInput').value = '';
	const table = document.getElementById('passwordTable');
	const noResults = document.getElementById('noResults');

	if (table) {
		table.style.display = '';
	}
	if (noResults) {
		noResults.style.display = 'none';
	}

	const rows = document.querySelectorAll('#passwordTable tbody tr');
	rows.forEach(row => {
		row.style.display = '';
	});
}

// Funzione per nascondere automaticamente i messaggi flash
function autoHideMessages() {
	const successMessage = document.getElementById('successMessage');
	const errorMessage = document.getElementById('errorMessage');

	if (successMessage) {
		setTimeout(() => {
			successMessage.style.opacity = '0';
			setTimeout(() => {
				successMessage.style.display = 'none';
			}, 500);
		}, 5000);
	}

	if (errorMessage) {
		setTimeout(() => {
			errorMessage.style.opacity = '0';
			setTimeout(() => {
				errorMessage.style.display = 'none';
			}, 500);
		}, 8000);
	}
}

// Inizializzazione
document.addEventListener('DOMContentLoaded', function() {
	// Nascondi le password all'inizializzazione
	const passwordDisplays = document.querySelectorAll('.password-display');
	passwordDisplays.forEach(display => {
		const originalPassword = display.textContent;
		display.setAttribute('data-password', originalPassword);

		const displayLength = Math.min(originalPassword.length, 20);
		display.textContent = '•'.repeat(displayLength);
		if (originalPassword.length > 20) {
			display.textContent += '...';
		}
	});

	// Assicurati che la tabella sia visibile all'inizio
	const table = document.getElementById('passwordTable');
	const noResults = document.getElementById('noResults');
	if (table && noResults) {
		table.style.display = '';
		noResults.style.display = 'none';
	}

	// Ricerca in tempo reale
	const searchInput = document.getElementById('searchInput');
	if (searchInput) {
		searchInput.addEventListener('input', filterTable);
	}

	// Nascondi automaticamente i messaggi flash
	autoHideMessages();
});

// Gestione cambio dimensione pagina
function changePageSize(size) {
	const url = new URL(window.location.href);
	url.searchParams.set('size', size);
	url.searchParams.set('page', 0); // Torna alla prima pagina

	window.location.href = url.toString();
}

// Gestione ricerca con paginazione
function searchPasswords() {
	const searchInput = document.getElementById('searchInput');
	const query = searchInput.value.trim();
	const url = new URL(window.location.href);

	if (query) {
		url.searchParams.set('search', query);
	} else {
		url.searchParams.delete('search');
	}

	// Reset a pagina 0 quando si cerca
	url.searchParams.set('page', 0);

	window.location.href = url.toString();
}

// Pulisci ricerca
function clearSearch() {
	const url = new URL(window.location.href);
	url.searchParams.delete('search');
	url.searchParams.set('page', 0);

	window.location.href = url.toString();
}

// Shortcut per tasto Enter nella ricerca
document.addEventListener('DOMContentLoaded', function() {
	const searchInput = document.getElementById('searchInput');
	if (searchInput) {
		searchInput.addEventListener('keypress', function(e) {
			if (e.key === 'Enter') {
				searchPasswords();
			}
		});
	}

	// Aggiorna l'input di ricerca se c'è una query in corso
	const urlParams = new URLSearchParams(window.location.search);
	const searchQuery = urlParams.get('search');
	if (searchQuery && searchInput) {
		searchInput.value = searchQuery;
	}
});

// Funzione per navigare alle pagine
function goToPage(pageNumber) {
    const url = new URL(window.location.href);
    const params = new URLSearchParams(url.search);
    
    // Mantieni tutti i parametri, aggiorna solo la pagina
    params.set('page', pageNumber);
    
    url.search = params.toString();
    window.location.href = url.toString();
}

// Funzione per cambiare dimensione pagina
function changePageSize(size) {
    const url = new URL(window.location.href);
    const params = new URLSearchParams(url.search);
    
    params.set('size', size);
    params.set('page', 0); // Torna alla prima pagina
    
    url.search = params.toString();
    window.location.href = url.toString();
}

// Funzione per ricerca
function searchPasswords() {
    const searchInput = document.getElementById('searchInput');
    const query = searchInput.value.trim();
    const url = new URL(window.location.href);
    const params = new URLSearchParams(url.search);
    
    if (query) {
        params.set('search', query);
    } else {
        params.delete('search');
    }
    
    // Reset a pagina 0 quando si cerca
    params.set('page', 0);
    
    url.search = params.toString();
    window.location.href = url.toString();
}

// Pulisci ricerca
function clearSearch() {
    const url = new URL(window.location.href);
    const params = new URLSearchParams(url.search);
    
    params.delete('search');
    params.set('page', 0);
    
    url.search = params.toString();
    window.location.href = url.toString();
}

// Inizializzazione al caricamento
document.addEventListener('DOMContentLoaded', function() {
    // Shortcut per tasto Enter nella ricerca
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchPasswords();
            }
        });
    }
    
    // Aggiorna l'input di ricerca se c'è una query in corso
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    if (searchQuery && searchInput) {
        searchInput.value = searchQuery;
    }
});

function exportEncrypted() {
    fetch('/api/export/encrypted')
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `passwords-backup-${new Date().toISOString().split('T')[0]}.enc`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            alert('✅ Backup cifrato esportato con successo!');
        })
        .catch(error => {
            console.error('Errore export:', error);
            alert('❌ Errore durante l\'export: ' + error.message);
        });
}

function exportCsv() {
    fetch('/api/export/csv')
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `passwords-metadata-${new Date().toISOString().split('T')[0]}.csv`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            alert('✅ CSV esportato con successo!');
        })
        .catch(error => {
            console.error('Errore export CSV:', error);
            alert('❌ Errore durante l\'export CSV: ' + error.message);
        });
}

function exportJson() {
    fetch('/api/export/json')
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `passwords-data-${new Date().toISOString().split('T')[0]}.json`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            alert('✅ JSON esportato con successo!');
        })
        .catch(error => {
            console.error('Errore export JSON:', error);
            alert('❌ Errore durante l\'export JSON: ' + error.message);
        });
}

function triggerImport(type) {
    const fileInput = document.getElementById('importFile');
    if (!fileInput) {
        console.error('Input file non trovato');
        return;
    }
    
    fileInput.accept = type === 'encrypted' ? '.enc' : '.csv';
    fileInput.onchange = function(e) {
        if (e.target.files && e.target.files[0]) {
            handleImportFile(e.target.files[0], type);
        }
    };
    fileInput.click();
}

function handleImportFile(file, type) {
    const overwrite = document.getElementById('overwriteCheckbox')?.checked || false;
    const formData = new FormData();
    formData.append('file', file);
    
    const endpoint = type === 'encrypted' ? '/api/import/encrypted' : '/api/import/csv';
    
    fetch(`${endpoint}?overwrite=${overwrite}`, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(result => {
        if (result.success) {
            alert(`✅ Import completato!\nImportati: ${result.importedCount}\nSaltati: ${result.skippedCount}\nErrori: ${result.errorCount}`);
            location.reload(); // Ricarica la pagina
        } else {
            alert(`❌ Import fallito: ${result.message}`);
        }
    })
    .catch(error => {
        console.error('Errore import:', error);
        alert('❌ Errore durante l\'import: ' + error.message);
    });
}

// Funzione di utility per mostrare notifiche
function showToast(message, type = 'info') {
    try {
        // Rimuovi toast esistenti
        document.querySelectorAll('.custom-toast').forEach(toast => toast.remove());
        
        const toast = document.createElement('div');
        toast.className = 'custom-toast';
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed; top: 20px; right: 20px; 
            padding: 15px; border-radius: 5px; z-index: 9999;
            background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#007bff'};
            color: white; font-weight: bold; box-shadow: 0 2px 10px rgba(0,0,0,0.2);
            max-width: 400px; word-wrap: break-word;
        `;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 5000);
    } catch (e) {
        console.error('Errore in showToast:', e);
        alert(message); // Fallback se showToast fallisce
    }
}

// Chiudi i menu quando si clicca altrove
document.addEventListener('click', function(event) {
    const exportMenu = document.getElementById('exportMenu');
    const importMenu = document.getElementById('importMenu');
    const exportBtn = document.querySelector('.export-btn');
    const importBtn = document.querySelector('.import-btn');
    
    // Se si clicca fuori dai menu, chiudili
    if (exportMenu && exportMenu.style.display === 'block') {
        const clickedInExport = exportMenu.contains(event.target) || 
                               (exportBtn && exportBtn.contains(event.target));
        if (!clickedInExport) {
            exportMenu.style.display = 'none';
        }
    }
    
    if (importMenu && importMenu.style.display === 'block') {
        const clickedInImport = importMenu.contains(event.target) || 
                               (importBtn && importBtn.contains(event.target));
        if (!clickedInImport) {
            importMenu.style.display = 'none';
        }
    }
});

// Gestione dei dropdown
let currentOpenMenu = null;

function toggleExportMenu() {
    const menu = document.getElementById('exportMenu');
    if (menu) {
        const importMenu = document.getElementById('importMenu');
        if (importMenu) importMenu.style.display = 'none';
        menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
    }
}

function toggleImportMenu() {
    const menu = document.getElementById('importMenu');
    if (menu) {
        const exportMenu = document.getElementById('exportMenu');
        if (exportMenu) exportMenu.style.display = 'none';
        menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
    }
}

// Chiudi i menu quando si clicca altrove
document.addEventListener('click', function(event) {
    const exportMenu = document.getElementById('exportMenu');
    const importMenu = document.getElementById('importMenu');
    const exportBtn = document.querySelector('.export-btn');
    const importBtn = document.querySelector('.import-btn');
    
    // Verifica che gli elementi esistano prima di usare contains()
    if (exportMenu && exportBtn) {
        const isClickInsideExport = exportMenu.contains(event.target) || 
                                    exportBtn.contains(event.target);
        
        if (!isClickInsideExport) {
            exportMenu.style.display = 'none';
            if (currentOpenMenu === 'export') currentOpenMenu = null;
        }
    }
    
    if (importMenu && importBtn) {
        const isClickInsideImport = importMenu.contains(event.target) || 
                                    importBtn.contains(event.target);
        
        if (!isClickInsideImport) {
            importMenu.style.display = 'none';
            if (currentOpenMenu === 'import') currentOpenMenu = null;
        }
    }
});

// Chiudi i menu quando si preme ESC
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        const exportMenu = document.getElementById('exportMenu');
        const importMenu = document.getElementById('importMenu');
        
        if (exportMenu) exportMenu.style.display = 'none';
        if (importMenu) importMenu.style.display = 'none';
        currentOpenMenu = null;
    }
});

