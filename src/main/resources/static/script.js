document.addEventListener('DOMContentLoaded', function() {
    fetchSectors().then(() => {
        setupFormSubmission();
        checkSession(true);
        setupSessionReset();
    });
});

function fetchSectors() {
    return fetch('/api/sectors')
        .then(response => {
            if (!response.ok) {
                throw new Error('fetchSectors error');
            }
            return response.json();
        })
        .then(sectors => {
            populateSectorsDropdown(sectors);
        })
        .catch(error => {
            console.error('error:', error);
        });
}

function populateSectorsDropdown(sectors, level = 0) {
    const selectElement = document.getElementById('sectors');

    if (level === 0) {
        selectElement.innerHTML = '';
    }

    sectors.forEach(sector => {
        const option = document.createElement('option');
        option.value = sector.id;

        let indent = '';
        for (let i = 0; i < level; i++) {
            indent += '&nbsp;&nbsp;&nbsp;&nbsp;';
        }

        option.innerHTML = indent + sector.name;
        selectElement.appendChild(option);

        if (sector.children && sector.children.length > 0) {
            populateSectorsDropdown(sector.children, level + 1);
        }
    });
}

function setupFormSubmission() {
    const form = document.querySelector('form');

    // errors
    const nameError = document.getElementById('nameError');
    const sectorsError = document.getElementById('sectorsError');
    const agreeError = document.getElementById('agreeError');

    // elements
    const nameInput = document.getElementById('name');
    const sectorsSelect = document.getElementById('sectors');
    const agreeCheckbox = document.querySelector('input[name="agree"]');
    const submitButton = document.querySelector('button[type="submit"]');

    const responseMessage = document.getElementById('responseMessage');

    form.addEventListener('submit', function(event) {
        event.preventDefault();

        // clear errors
        nameError.style.display = 'none';
        sectorsError.style.display = 'none';
        agreeError.style.display = 'none';
        responseMessage.style.display = 'none';

        let isValid = true;

        // validate name
        if (!nameInput.value.trim()) {
            nameError.textContent = 'Please enter your name';
            nameError.style.display = 'block';
            isValid = false;
        }

        // validate sectors
        if (sectorsSelect.selectedOptions.length === 0) {
            sectorsError.textContent = 'Please select at least one sector';
            sectorsError.style.display = 'block';
            isValid = false;
        }

        // validate agree to terms
        if (!agreeCheckbox.checked) {
            agreeError.textContent = 'You must agree to the terms';
            agreeError.style.display = 'block';
            isValid = false;
        }

        if (isValid) {
            const formData = {
                name: nameInput.value,
                sectors: Array.from(sectorsSelect.selectedOptions).map(option => parseInt(option.value)),
                agreeToTerms: agreeCheckbox.checked
            };

            // update user
            const userId = localStorage.getItem('userId');
            if (userId) {
                formData.id = parseInt(userId);
            }

            console.log('data:', formData);
            const url = userId ? `/api/users/${userId}` : '/api/users';

            // Disable submit button while saving
            submitButton.disabled = true;
            submitButton.textContent = 'Saving...';

            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            })
            .then(response => response.json())
            .then(data => {
                // Re-enable submit button
                submitButton.disabled = false;
                submitButton.textContent = 'Save';

                if (data.status === 'error') {
                    responseMessage.textContent = 'Error: ' + Object.values(data).filter(v => typeof v === 'string' && v !== 'error').join(', ');
                    responseMessage.style.backgroundColor = '#ffdddd';
                    responseMessage.style.color = 'red';
                    responseMessage.style.display = 'block';

                } else {
                    // success
                    if(userId){
                        responseMessage.textContent = 'User updated successfully!';
                    } else {
                        responseMessage.textContent = 'User created successfully!';
                    }
                    responseMessage.style.backgroundColor = '#ddffdd';
                    responseMessage.style.color = 'green';
                    responseMessage.style.display = 'block';

                    if (!userId && data.id) {
                        localStorage.setItem('userId', data.id);
                        console.log('saved session:', data.id);
                        checkSession();
                    }

                    fillFormWithUserData(data);
                }
            })
            .catch(error => {
                // Re-enable submit button
                submitButton.disabled = false;
                submitButton.textContent = 'Save';

                console.error('Error:', error);
                responseMessage.textContent = 'error occured';
                responseMessage.style.backgroundColor = '#ffdddd';
                responseMessage.style.color = 'red';
                responseMessage.style.display = 'block';
            });
        }
    });
}

function fillFormWithUserData(userData) {
    if (!userData) return;

    const nameInput = document.getElementById('name');
    if (nameInput && userData.name) {
        nameInput.value = userData.name;
    }

    const sectorsSelect = document.getElementById('sectors');
    if (sectorsSelect && userData.sectors && userData.sectors.length > 0) {
        Array.from(sectorsSelect.options).forEach(option => {
            option.selected = false;
        });

        userData.sectors.forEach(sectorId => {
            const option = sectorsSelect.querySelector(`option[value="${sectorId}"]`);
            if (option) {
                option.selected = true;
            }
        });
    }

    const agreeCheckbox = document.querySelector('input[name="agree"]');
    if (agreeCheckbox && userData.agreeToTerms !== undefined) {
        agreeCheckbox.checked = userData.agreeToTerms;
    }
}

function setupSessionReset() {
    const newSessionBtn = document.getElementById('newSessionBtn');

    if (newSessionBtn) {
        newSessionBtn.addEventListener('click', function() {
            localStorage.removeItem('userId');
            window.location.reload();
        });
    }
}

function checkSession(initial = false) {
    const userId = localStorage.getItem('userId');
    const newSessionBtn = document.getElementById('newSessionBtn');

    if (userId) {
        if (newSessionBtn) {
            newSessionBtn.style.display = 'block';
        }
        if(initial) {
            getSavedUser(userId);
        }
    } else {
        if (newSessionBtn) {
            newSessionBtn.style.display = 'none';
        }
    }
}

async function getSavedUser(id) {
    const responseMessage = document.getElementById('responseMessage');
    try {
        const url = `/api/users/${id}`;
        const response = await fetch(url);
        if (!response.ok) {
            responseMessage.textContent = 'Error loading saved user data';
            responseMessage.style.backgroundColor = '#ffdddd';
            responseMessage.style.color = 'red';
            responseMessage.style.display = 'block';
        }
        const data =  await response.json();
        fillFormWithUserData(data);

        responseMessage.textContent = 'Saved user data loaded successfully';
        responseMessage.style.backgroundColor = '#ddffdd';
        responseMessage.style.color = 'green';
        responseMessage.style.display = 'block';
    } catch (error) {
        responseMessage.textContent = 'Error loading saved user data';
        responseMessage.style.backgroundColor = '#ffdddd';
        responseMessage.style.color = 'red';
        responseMessage.style.display = 'block';
    }
}
