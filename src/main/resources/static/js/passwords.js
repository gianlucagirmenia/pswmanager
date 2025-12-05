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