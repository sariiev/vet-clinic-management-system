let selectedVeterinarianId = null;
let currentStep = 'step-search';
let scheduleBackStep = 'step-search';
let bookedSlots = [];

document.addEventListener("click", function(event) {
    const target = event.target;
    if (target.id === 'cancel-button' || target.id === 'base-overlay') {
        const innerDialog = document.getElementById('dialog-container');
        innerDialog.innerHTML = '';
        return;
    }

    const veterinarian = target.closest('#veterinarian-search-results-container .result-card');
    if (veterinarian) {
        document.querySelectorAll('#veterinarian-search-results-container .result-card.selected')
            .forEach(card => card.classList.remove('selected'));
        veterinarian.classList.add('selected');
        selectedVeterinarianId = veterinarian.dataset.veterinarianId;
    }

    const segmentedOption = target.closest('.segmented-option');
    if (segmentedOption) {
        document.querySelectorAll('#step-search .search-panel').forEach(p => {
            p.style.display = 'none';
        })
        document.querySelectorAll('.segmented-option').forEach(p => {
            p.classList.remove('active');
        })
        document.getElementById(segmentedOption.dataset.target).style.display = 'block';
        segmentedOption.classList.add('active');

        document.getElementById('search-results-container').innerHTML = '';
        document.getElementById('owner-search-input').value = '';
        document.getElementById('pet-search-input').value = '';

        if (segmentedOption.dataset.target === 'owner-search-panel') {
            document.getElementById('register-actions').style.display = '';
        } else {
            document.getElementById('register-actions').style.display = 'none';
        }

        return;
    }

    if (target.id === 'back-button') {
        if (currentStep === 'step-pets') {
            showStep('step-search');
        } else if (currentStep === 'step-schedule') {
            const calendar = document.getElementById('calendar');
            calendar._flatpickr.clear();
            document.getElementById('day-section').style.display = 'none';
            document.getElementById('confirm-button').disabled = true;
            document.getElementById('scheduling-error').textContent = '';
            document.getElementById('availability').textContent = '';
            showStep(scheduleBackStep);
        }
        return;
    }

    if (target.id === 'confirm-button') {
        onConfirm();
    }

    if (target.id === 'cancel-nested-button') {
        document.getElementById('nested-dialog-container').innerHTML = '';
        return;
    }

    if (target.id === 'add-phone') {
        const container = document.getElementById('phone-numbers');
        const row = document.createElement('div');
        row.className = 'phone-row'
        row.innerHTML = '<input type="text" name="phoneNumbers" required/>' + '<button type="button" class="btn btn-secondary js-remove-phone">-</button>';
        container.appendChild(row);
        return;
    }

    const removePhone = target.closest('.js-remove-phone');
    if (removePhone) {
        const rows = document.querySelectorAll('#phone-numbers .phone-row');
        if (rows.length > 1) {
            removePhone.closest('.phone-row').remove();
        }
        return;
    }
});

document.body.addEventListener('htmx:afterSwap', function (event) {
    const target = event.detail.target;

    if (target && target.id === 'veterinarian-search-results-container' && selectedVeterinarianId) {
        const card = target.querySelector(`.result-card[data-veterinarian-id="${selectedVeterinarianId}"]`);
        if (card) {
            card.classList.add('selected');
        }
    }

    if (target && target.id === 'dialog-container') {
        showStep('step-search')
        const calendar = document.getElementById('calendar');
        if (calendar && !calendar._flatpickr) {
            flatpickr(
                calendar, {
                    inline: true,
                    minDate: calendar.dataset.minDate,
                    maxDate: calendar.dataset.maxDate,
                    altFormat: "F j, Y",
                    dateFormat: "Y-m-d",
                    onChange: function (selectedDates, dateStr) {
                        onDateSelected(dateStr);
                    }
                }
            )
        }
    }
    if (target && target.id === 'owner-pets-container') {
        document.getElementById('nested-dialog-container').innerHTML = '';
        showStep('step-pets')
    }
    if (target && target.id === 'selected-pet-container') {
        document.getElementById('nested-dialog-container').innerHTML = '';
        scheduleBackStep = currentStep;
        showStep('step-schedule');
    }
});

document.addEventListener('change', function (event) {
    if (event.target.id === 'appointment-start-time' || event.target.id === 'appointment-duration') {
        validateInterval();
    }
});

document.body.addEventListener('htmx:responseError', function (event) {
    if (event.detail.elt.closest('#nested-dialog-container')) {
        const registerError = document.getElementById('register-error');
        if (registerError) {
            registerError.innerHTML = event.detail.xhr.responseText;
        }
    }
});

function showStep(stepId) {
    ['step-search', 'step-pets', 'step-schedule'].forEach(s => {
        const element = document.getElementById(s);
        if (element) {
            element.style.display = 'none';
        }
    });

    currentStep = stepId;
    if (currentStep) {
        document.getElementById(currentStep).style.display = 'block';
    }

    if (currentStep !== 'step-search') {
        document.getElementById('back-button').style.display = 'inline-flex';
        document.getElementById('cancel-button').style.display = 'none';
    } else {
        document.getElementById('back-button').style.display = 'none';
        document.getElementById('cancel-button').style.display = 'inline-flex';
    }

    if (currentStep === 'step-schedule') {
        document.getElementById('confirm-button').style.display = 'inline-flex';
    } else {
        document.getElementById('confirm-button').style.display = 'none';
    }

    const modalSubtitle = document.getElementById('modal-subtitle');
    if (modalSubtitle) {
        const stepsToTitles = {
            'step-search': 'Find the pet owner',
            'step-pets': 'Select the pet',
            'step-schedule': 'Select the date'
        };
        modalSubtitle.textContent = stepsToTitles[currentStep];
    }

    const modal = document.querySelector('#base-overlay .modal');
    if (modal) {
        modal.scrollTop = 0;
    }
}

function onDateSelected(dateStr) {
    if (!dateStr) {
        return;
    }

    const daySection = document.getElementById('day-section');
    document.getElementById('selected-date').value = dateStr;

    const veterinarianId = document.getElementById('base-overlay').dataset.veterinarianId;
    fetch(`/veterinarians/${veterinarianId}/booked-slots?date=${dateStr}`)
        .then(res => res.json())
        .then(slots => {
            bookedSlots = slots.map(slot => ({
                startMinute: timeToMinutes(slot.startTime),
                endMinute: timeToMinutes(slot.endTime),
                startStr: slot.startTime,
                endStr: slot.endTime
            }));

            renderDaySchedule(bookedSlots);

            document.getElementById('appointment-start-time').value = '';
            document.getElementById('appointment-duration').value = '';
            document.getElementById('confirm-button').disabled = true;
            document.getElementById('availability').textContent = '';
            document.getElementById('scheduling-error').textContent = '';

            daySection.style.display = 'block';
        });
}

function renderDaySchedule(slots) {
    const container = document.getElementById('day-schedule');
    if (slots.length === 0) {
        container.innerHTML = '<p>No appointments</p>';
        return;
    }
    const rows = slots.map(slot => `<li>${slot.startStr} - ${slot.endStr}</li>`).join('');
    container.innerHTML = `<ul>${rows}</ul>`;
}

function validateInterval() {
    const startTime = document.getElementById('appointment-start-time').value;
    const duration = document.getElementById('appointment-duration').value;

    const confirmButton = document.getElementById('confirm-button');
    const error = document.getElementById('scheduling-error');
    const availability = document.getElementById('availability');

    confirmButton.disabled = true;
    error.textContent = '';
    availability.textContent = '';

    if (!startTime || !duration) {
        return;
    }

    const element = document.getElementById('step-schedule');
    const workingHoursStart = element.dataset.workingHoursStart;
    const workingHoursEnd = element.dataset.workingHoursEnd;
    const workingHoursStartMinute = timeToMinutes(workingHoursStart);
    const workingHoursEndMinute = timeToMinutes(workingHoursEnd);

    const durationSelect = document.getElementById('appointment-duration');
    const durationMinutes = Number(durationSelect.selectedOptions[0].dataset.durationMinutes);

    const startMinute = timeToMinutes(startTime);
    const expectedEndMinute = startMinute + durationMinutes;

    if (startMinute < workingHoursStartMinute || expectedEndMinute > workingHoursEndMinute) {
        error.textContent = `Appointment must be within working hours(${workingHoursStart} - ${workingHoursEnd})`;
        return;
    }

    const now = new Date();
    const nowHours = now.getHours();
    const nowMinutes = now.getMinutes();

    const selectedDate = document.getElementById('selected-date').value;
    const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;

    if (selectedDate === today) {
        if (startMinute < nowHours * 60 + nowMinutes) {
            error.textContent = 'Appointment cannot be scheduled for the past';
            return;
        }
    }

    const overlaps = bookedSlots.some(slot => startMinute < slot.endMinute && expectedEndMinute > slot.startMinute);
    if (overlaps) {
        error.textContent = 'Selected interval overlaps with an existing appointment';
        return;
    }

    availability.textContent = `✓ Time slot available: ${startTime} - ${minutesToTime(expectedEndMinute)} (${durationMinutes} minutes)`;
    confirmButton.disabled = false;
}

function minutesToTime(minutes) {
    return String(Math.floor(minutes / 60)).padStart(2, '0') + ':' + String(minutes % 60).padStart(2, '0');
}

function timeToMinutes(timeStr) {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + minutes;
}

function onConfirm() {
    const confirmButton = document.getElementById('confirm-button');
    confirmButton.disabled = true;
    confirmButton.textContent = 'Scheduling...';

    const veterinarianId = document.getElementById('base-overlay').dataset.veterinarianId;
    const petId = document.getElementById('selected-pet-id').value;
    const date = document.getElementById('selected-date').value;
    const startTime = document.getElementById('appointment-start-time').value;
    const duration = document.getElementById('appointment-duration').value;

    const params = new URLSearchParams();
    params.append('veterinarianId', veterinarianId);
    params.append('petId', petId);
    params.append('date', date);
    params.append('startTime', startTime);
    params.append('duration', duration);

    fetch('/appointments', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(res => res.text().then(html => ({ok: res.ok, html, email: res.headers.get('X-Owner-Email')})))
        .then(({ok, html, email}) => {
            if (ok) {
                const scheduleContainer = document.getElementById('schedule-container');
                scheduleContainer.innerHTML = html;
                htmx.process(scheduleContainer);
                document.getElementById('dialog-container').innerHTML = '';
                showNotification('Appointment scheduled successfully', email ? 'Confirmation email sent to ' + email : null);
            } else {
                confirmButton.disabled = false;
                confirmButton.textContent = 'Confirm';
                document.getElementById('scheduling-error').innerHTML = html;
            }
        });
}

function showNotification(title, text) {
    const container = document.getElementById('notification-container');
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.innerHTML = `<div><h4>${title}</h4>` + (text ? `<div>${text}</div></div>` : '');
    container.appendChild(notification);
    setTimeout(() => notification.remove(), 5000);
}