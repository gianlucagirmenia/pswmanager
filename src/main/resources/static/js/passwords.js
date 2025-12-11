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