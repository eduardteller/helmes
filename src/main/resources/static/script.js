// when page is loaded, setup functions are executed
document.addEventListener('DOMContentLoaded', function() {
    fetchSectors().then(() => {
        setupFormSubmission();
        checkSession(true);
        setupSessionReset();
    });
});

// fetches all the sectors from backend and populates the selector
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

// populates the sectors dropdown recursivley with the fetched sectors
function populateSectorsDropdown(sectors, level = 0) {
    const selectElement = document.getElementById('sectors');

    // clear the dropdown initially
    if (level === 0) {
        selectElement.innerHTML = '';
    }

    sectors.forEach(sector => {
        const option = document.createElement('option');
        option.value = sector.id;

        // indent depends on the level
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

    // error element references
    const nameError = document.getElementById('nameError');
    const sectorsError = document.getElementById('sectorsError');
    const agreeError = document.getElementById('agreeError');

    // form element references
    const nameInput = document.getElementById('name');
    const sectorsSelect = document.getElementById('sectors');
    const agreeCheckbox = document.querySelector('input[name="agree"]');
    const submitButton = document.querySelector('button[type="submit"]');

    // response message element reference
    const responseMessage = document.getElementById('responseMessage');

    // listen for form submission
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
            // if valid construct form data
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

            // prepare url for either create or update user
            const url = userId ? `/api/users/${userId}` : '/api/users';

            // disable submit button to prevent multiple submissions
            submitButton.disabled = true;
            submitButton.textContent = 'Saving...';

            // update/create via request backend api
            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            })
            .then(response => response.json())
            .then(data => {

                // enable submit button after response
                submitButton.disabled = false;
                submitButton.textContent = 'Save';

                //if error show red error message
                if (data.status === 'error') {
                    responseMessage.textContent = 'Error: ' + Object.values(data).filter(v => typeof v === 'string' && v !== 'error').join(', ');
                    responseMessage.style.backgroundColor = '#ffdddd';
                    responseMessage.style.color = 'red';
                    responseMessage.style.display = 'block';

                } else {
                    // if success show green success message
                    if(userId){
                        responseMessage.textContent = 'User updated successfully!';
                    } else {
                        responseMessage.textContent = 'User created successfully!';
                    }
                    responseMessage.style.backgroundColor = '#ddffdd';
                    responseMessage.style.color = 'green';
                    responseMessage.style.display = 'block';

                    // save userId to localStorage if not already saved
                    if (!userId && data.id) {
                        localStorage.setItem('userId', data.id);
                        console.log('saved session:', data.id);
                        checkSession();
                    }

                    // fill form with user data
                    fillFormWithUserData(data);
                }
            })
            .catch(error => {
                // enable submit button after error
                submitButton.disabled = false;
                submitButton.textContent = 'Save';

                // show error message
                console.error('Error:', error);
                responseMessage.textContent = 'error occured';
                responseMessage.style.backgroundColor = '#ffdddd';
                responseMessage.style.color = 'red';
                responseMessage.style.display = 'block';
            });
        }
    });
}

// fills the form with user data if available
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

// sets up the session reset button to clear localStorage and reload the page
function setupSessionReset() {
    const newSessionBtn = document.getElementById('newSessionBtn');

    if (newSessionBtn) {
        newSessionBtn.addEventListener('click', function() {
            localStorage.removeItem('userId');
            window.location.reload();
        });
    }
}

// checks if there is a session stored in localStorage and displays the reset button and fetches the user data if specified
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

// retrieves the saved user data from the backend using the userId stored in localStorage
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
