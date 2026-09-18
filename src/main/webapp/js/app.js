// =========================================================
// LIBRARY MANAGEMENT SYSTEM
// COMPLETE FRONTEND JAVASCRIPT
// =========================================================


// =========================================================
// API URLs
// =========================================================

const BOOK_API = "api/books";
const MEMBER_API = "api/members";
const ISSUE_API = "api/issues";
const RETURN_API = "api/returns";


// =========================================================
// SECTION NAVIGATION
// =========================================================

function showSection(sectionId, button = null) {

    document
        .querySelectorAll("main section")
        .forEach(section => {

            section.classList.add("hidden");

        });


    const section =
        document.getElementById(sectionId);


    if (!section) {
        return;
    }


    section.classList.remove("hidden");


    document
        .querySelectorAll(".nav-btn")
        .forEach(btn => {

            btn.classList.remove("active");

        });


    if (button) {

        button.classList.add("active");

    } else {

        const navButton =
            document.querySelector(
                `.nav-btn[data-section="${sectionId}"]`
            );

        if (navButton) {
            navButton.classList.add("active");
        }

    }


    // Load required data

    if (sectionId === "dashboard") {

        loadDashboard();

    }


    if (sectionId === "books") {

        loadBooks();

    }


    if (sectionId === "members") {

        loadMembers();

    }


    if (sectionId === "issue") {

        loadIssuePage();

    }


    if (sectionId === "return") {

        loadReturns();

    }

}


// Quick action navigation

function showSectionByName(sectionId) {

    showSection(sectionId);

}


// =========================================================
// TOAST
// =========================================================

function showToast(message, type = "success") {

    const toast =
        document.getElementById("toast");


    if (!toast) {
        return;
    }


    toast.textContent = message;


    toast.className =
        `toast ${type}`;


    clearTimeout(
        window.toastTimer
    );


    window.toastTimer =
        setTimeout(() => {

            toast.classList.add("hidden");

        }, 3000);

}


// =========================================================
// SAFE HTML
// =========================================================

function escapeHtml(value) {

    if (value === null ||
        value === undefined) {

        return "";
    }


    return String(value)

        .replaceAll("&", "&amp;")

        .replaceAll("<", "&lt;")

        .replaceAll(">", "&gt;")

        .replaceAll('"', "&quot;")

        .replaceAll("'", "&#039;");
}


// =========================================================
// DASHBOARD
// =========================================================

async function loadDashboard() {

    try {

        const [
            booksResponse,
            membersResponse,
            issuesResponse
        ] = await Promise.all([

            fetch(BOOK_API),

            fetch(MEMBER_API),

            fetch(ISSUE_API)

        ]);


        if (!booksResponse.ok) {

            throw new Error(
                "Unable to load books"
            );

        }


        if (!membersResponse.ok) {

            throw new Error(
                "Unable to load members"
            );

        }


        if (!issuesResponse.ok) {

            throw new Error(
                "Unable to load issues"
            );

        }


        const books =
            await booksResponse.json();


        const members =
            await membersResponse.json();


        const issues =
            await issuesResponse.json();


        let totalCopies = 0;

        let availableCopies = 0;


        books.forEach(book => {

            totalCopies +=
                Number(book.quantity || 0);


            availableCopies +=
                Number(
                    book.available_quantity || 0
                );

        });


        const issuedCopies =
            totalCopies -
            availableCopies;


        document.getElementById(
            "totalBooks"
        ).textContent =
            totalCopies;


        document.getElementById(
            "availableBooks"
        ).textContent =
            availableCopies;


        document.getElementById(
            "totalMembers"
        ).textContent =
            members.length;


        document.getElementById(
            "issuedBooks"
        ).textContent =
            issues.length;


        document.getElementById(
            "collectionStatus"
        ).textContent =
            `${books.length} titles`;


        document.getElementById(
            "availableStatus"
        ).textContent =
            `${availableCopies} copies`;


        document.getElementById(
            "issuedStatus"
        ).textContent =
            `${issuedCopies} copies`;


        document.getElementById(
            "memberStatus"
        ).textContent =
            `${members.length} members`;


        loadRecentBooks(books);


    } catch (error) {

        console.error(error);

        document.getElementById(
            "collectionStatus"
        ).textContent =
            "Unable to load";


        document.getElementById(
            "availableStatus"
        ).textContent =
            "Unable to load";


        document.getElementById(
            "issuedStatus"
        ).textContent =
            "Unable to load";


        document.getElementById(
            "memberStatus"
        ).textContent =
            "Unable to load";

    }

}


// =========================================================
// RECENT BOOKS
// =========================================================

function loadRecentBooks(books) {

    const container =
        document.getElementById(
            "recentBooks"
        );


    if (!container) {
        return;
    }


    if (!books ||
        books.length === 0) {

        container.innerHTML =
            `<div class="empty-message">
                No books added yet.
             </div>`;

        return;
    }


    const recentBooks =
        books.slice(0, 5);


    container.innerHTML =
        recentBooks.map(book => `

            <div class="recent-book">

                <div class="recent-book-info">

                    <strong>
                        ${escapeHtml(book.title)}
                    </strong>

                    <small>
                        ${escapeHtml(book.author)}
                    </small>

                </div>

                <div class="recent-book-quantity">

                    ${book.available_quantity}
                    /
                    ${book.quantity}

                    available

                </div>

            </div>

        `).join("");

}


// =========================================================
// BOOKS
// =========================================================

async function loadBooks() {

    const table =
        document.getElementById(
            "bookTableBody"
        );


    if (!table) {
        return;
    }


    try {

        const search =
            document.getElementById(
                "search"
            )?.value || "";


        let url =
            BOOK_API;


        if (search.trim()) {

            url +=
                "?search=" +
                encodeURIComponent(
                    search.trim()
                );

        }


        const response =
            await fetch(url);


        if (!response.ok) {

            throw new Error(
                "Unable to load books"
            );

        }


        const books =
            await response.json();


        table.innerHTML = "";


        if (books.length === 0) {

            table.innerHTML = `

                <tr>

                    <td
                        colspan="8"
                        class="empty-message">

                        No books found.

                    </td>

                </tr>

            `;

            return;

        }


        books.forEach(book => {

            const row =
                document.createElement("tr");


            row.innerHTML = `

                <td>
                    ${book.id}
                </td>

                <td>
                    ${escapeHtml(book.isbn)}
                </td>

                <td>
                    <strong>
                        ${escapeHtml(book.title)}
                    </strong>
                </td>

                <td>
                    ${escapeHtml(book.author)}
                </td>

                <td>
                    ${escapeHtml(book.category || "-")}
                </td>

                <td>
                    ${book.quantity}
                </td>

                <td>

                    <span class="status-badge
                        ${
                            Number(book.available_quantity) > 0
                                ? "status-available"
                                : "status-issued"
                        }">

                        ${book.available_quantity}

                    </span>

                </td>

                <td>

                    <button
                        class="edit-btn"
                        onclick="editBook(${book.id})">

                        Edit

                    </button>


                    <button
                        class="delete-btn"
                        onclick="deleteBook(${book.id})">

                        Delete

                    </button>

                </td>

            `;


            table.appendChild(row);

        });


    } catch (error) {

        console.error(error);

        table.innerHTML = `

            <tr>

                <td
                    colspan="8"
                    class="empty-message">

                    Unable to load books.

                </td>

            </tr>

        `;

    }

}


// =========================================================
// BOOK MODAL
// =========================================================

function openBookModal() {

    document
        .getElementById("bookModal")
        .classList.remove("hidden");


    document.getElementById(
        "bookModalTitle"
    ).textContent =
        "Add New Book";


    document.getElementById(
        "bookForm"
    ).reset();


    document.getElementById(
        "bookId"
    ).value = "";

}


function closeBookModal() {

    document
        .getElementById("bookModal")
        .classList.add("hidden");

}


// =========================================================
// ADD / EDIT BOOK
// =========================================================

document
    .getElementById("bookForm")
    ?.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const id =
                document.getElementById(
                    "bookId"
                ).value;


            const formData =
                new URLSearchParams();


            formData.append(
                "isbn",
                document.getElementById(
                    "isbn"
                ).value.trim()
            );


            formData.append(
                "title",
                document.getElementById(
                    "title"
                ).value.trim()
            );


            formData.append(
                "author",
                document.getElementById(
                    "author"
                ).value.trim()
            );


            formData.append(
                "category",
                document.getElementById(
                    "category"
                ).value.trim()
            );


            formData.append(
                "quantity",
                document.getElementById(
                    "quantity"
                ).value
            );


            try {

                let response;


                if (id) {

                    formData.append(
                        "id",
                        id
                    );


                    response =
                        await fetch(
                            BOOK_API,
                            {
                                method: "PUT",

                                headers: {
                                    "Content-Type":
                                        "application/x-www-form-urlencoded"
                                },

                                body: formData
                            }
                        );

                } else {

                    response =
                        await fetch(
                            BOOK_API,
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/x-www-form-urlencoded"
                                },

                                body: formData
                            }
                        );

                }


                const result =
                    await response.json();


                if (!response.ok ||
                    !result.success) {

                    throw new Error(
                        result.message ||
                        "Unable to save book"
                    );

                }


                showToast(
                    result.message ||
                    "Book saved successfully"
                );


                closeBookModal();


                await loadBooks();

                await loadDashboard();


            } catch (error) {

                console.error(error);

                showToast(
                    error.message,
                    "error"
                );

            }

        }
    );


// =========================================================
// EDIT BOOK
// =========================================================

async function editBook(id) {

    try {

        const response =
            await fetch(BOOK_API);


        const books =
            await response.json();


        const book =
            books.find(
                item =>
                    Number(item.id) === Number(id)
            );


        if (!book) {

            showToast(
                "Book not found",
                "error"
            );

            return;
        }


        openBookModal();


        document.getElementById(
            "bookModalTitle"
        ).textContent =
            "Edit Book";


        document.getElementById(
            "bookId"
        ).value =
            book.id;


        document.getElementById(
            "isbn"
        ).value =
            book.isbn || "";


        document.getElementById(
            "title"
        ).value =
            book.title || "";


        document.getElementById(
            "author"
        ).value =
            book.author || "";


        document.getElementById(
            "category"
        ).value =
            book.category || "";


        document.getElementById(
            "quantity"
        ).value =
            book.quantity || 1;


    } catch (error) {

        console.error(error);

        showToast(
            "Unable to open book",
            "error"
        );

    }

}


// =========================================================
// DELETE BOOK
// =========================================================

async function deleteBook(id) {

    if (!confirm(
        "Are you sure you want to delete this book?"
    )) {

        return;

    }


    try {

        const response =
            await fetch(
                `${BOOK_API}?id=${id}`,
                {
                    method: "DELETE"
                }
            );


        const result =
            await response.json();


        if (!response.ok ||
            !result.success) {

            throw new Error(
                result.message ||
                "Unable to delete book"
            );

        }


        showToast(
            result.message ||
            "Book deleted"
        );


        await loadBooks();

        await loadDashboard();


    } catch (error) {

        console.error(error);

        showToast(
            error.message,
            "error"
        );

    }

}


// =========================================================
// MEMBERS
// =========================================================

async function loadMembers() {

    const table =
        document.getElementById(
            "memberTableBody"
        );


    if (!table) {
        return;
    }


    try {

        const search =
            document.getElementById(
                "memberSearch"
            )?.value || "";


        let url =
            MEMBER_API;


        if (search.trim()) {

            url +=
                "?search=" +
                encodeURIComponent(
                    search.trim()
                );

        }


        const response =
            await fetch(url);


        if (!response.ok) {

            throw new Error(
                "Unable to load members"
            );

        }


        const members =
            await response.json();


        table.innerHTML = "";


        if (members.length === 0) {

            table.innerHTML = `

                <tr>

                    <td
                        colspan="6"
                        class="empty-message">

                        No members found.

                    </td>

                </tr>

            `;

            return;

        }


        members.forEach(member => {

            const row =
                document.createElement("tr");


            row.innerHTML = `

                <td>
                    ${member.id}
                </td>

                <td>

                    <strong>
                        ${escapeHtml(member.name)}
                    </strong>

                </td>

                <td>
                    ${escapeHtml(
                        member.email || "-"
                    )}
                </td>

                <td>
                    ${escapeHtml(
                        member.phone || "-"
                    )}
                </td>

                <td>
                    ${escapeHtml(
                        member.address || "-"
                    )}
                </td>

                <td>

                    <button
                        class="edit-btn"
                        onclick="editMember(${member.id})">

                        Edit

                    </button>


                    <button
                        class="delete-btn"
                        onclick="deleteMember(${member.id})">

                        Delete

                    </button>

                </td>

            `;


            table.appendChild(row);

        });


    } catch (error) {

        console.error(error);

        table.innerHTML = `

            <tr>

                <td
                    colspan="6"
                    class="empty-message">

                    Unable to load members.

                </td>

            </tr>

        `;

    }

}


// =========================================================
// MEMBER MODAL
// =========================================================

function openMemberModal() {

    document
        .getElementById("memberModal")
        .classList.remove("hidden");


    document.getElementById(
        "memberModalTitle"
    ).textContent =
        "Add New Member";


    document.getElementById(
        "memberForm"
    ).reset();


    document.getElementById(
        "memberId"
    ).value = "";

}


function closeMemberModal() {

    document
        .getElementById("memberModal")
        .classList.add("hidden");

}


// =========================================================
// ADD / EDIT MEMBER
// =========================================================

document
    .getElementById("memberForm")
    ?.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const id =
                document.getElementById(
                    "memberId"
                ).value;


            const formData =
                new URLSearchParams();


            formData.append(
                "name",
                document.getElementById(
                    "memberName"
                ).value.trim()
            );


            formData.append(
                "email",
                document.getElementById(
                    "memberEmail"
                ).value.trim()
            );


            formData.append(
                "phone",
                document.getElementById(
                    "memberPhone"
                ).value.trim()
            );


            formData.append(
                "address",
                document.getElementById(
                    "memberAddress"
                ).value.trim()
            );


            try {

                let response;


                if (id) {

                    formData.append(
                        "id",
                        id
                    );


                    response =
                        await fetch(
                            MEMBER_API,
                            {
                                method: "PUT",

                                headers: {
                                    "Content-Type":
                                        "application/x-www-form-urlencoded"
                                },

                                body: formData
                            }
                        );

                } else {

                    response =
                        await fetch(
                            MEMBER_API,
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/x-www-form-urlencoded"
                                },

                                body: formData
                            }
                        );

                }


                const result =
                    await response.json();


                if (!response.ok ||
                    !result.success) {

                    throw new Error(
                        result.message ||
                        "Unable to save member"
                    );

                }


                showToast(
                    result.message ||
                    "Member saved successfully"
                );


                closeMemberModal();


                await loadMembers();

                await loadDashboard();


            } catch (error) {

                console.error(error);

                showToast(
                    error.message,
                    "error"
                );

            }

        }
    );


// =========================================================
// EDIT MEMBER
// =========================================================

async function editMember(id) {

    try {

        const response =
            await fetch(MEMBER_API);


        const members =
            await response.json();


        const member =
            members.find(
                item =>
                    Number(item.id) === Number(id)
            );


        if (!member) {

            showToast(
                "Member not found",
                "error"
            );

            return;

        }


        openMemberModal();


        document.getElementById(
            "memberModalTitle"
        ).textContent =
            "Edit Member";


        document.getElementById(
            "memberId"
        ).value =
            member.id;


        document.getElementById(
            "memberName"
        ).value =
            member.name || "";


        document.getElementById(
            "memberEmail"
        ).value =
            member.email || "";


        document.getElementById(
            "memberPhone"
        ).value =
            member.phone || "";


        document.getElementById(
            "memberAddress"
        ).value =
            member.address || "";


    } catch (error) {

        console.error(error);

        showToast(
            "Unable to open member",
            "error"
        );

    }

}


// =========================================================
// DELETE MEMBER
// =========================================================

async function deleteMember(id) {

    if (!confirm(
        "Are you sure you want to delete this member?"
    )) {

        return;

    }


    try {

        const response =
            await fetch(
                `${MEMBER_API}?id=${id}`,
                {
                    method: "DELETE"
                }
            );


        const result =
            await response.json();


        if (!response.ok ||
            !result.success) {

            throw new Error(
                result.message ||
                "Unable to delete member"
            );

        }


        showToast(
            result.message ||
            "Member deleted"
        );


        await loadMembers();

        await loadDashboard();


    } catch (error) {

        console.error(error);

        showToast(
            error.message,
            "error"
        );

    }

}


// =========================================================
// ISSUE BOOK PAGE
// =========================================================

async function loadIssuePage() {

    await loadIssueBooks();

    await loadIssueMembers();

    setDefaultIssueDates();

    updateIssueSummary();

}


// =========================================================
// LOAD AVAILABLE BOOKS
// =========================================================

async function loadIssueBooks() {

    const select =
        document.getElementById(
            "issueBook"
        );


    if (!select) {
        return;
    }


    try {

        const response =
            await fetch(BOOK_API);


        const books =
            await response.json();


        const availableBooks =
            books.filter(
                book =>
                    Number(
                        book.available_quantity
                    ) > 0
            );


        select.innerHTML =
            `<option value="">
                Select a book
             </option>`;


        availableBooks.forEach(book => {

            const option =
                document.createElement(
                    "option"
                );


            option.value =
                book.id;


            option.textContent =
                `${book.title} — ${book.author} (${book.available_quantity} available)`;


            option.dataset.title =
                book.title;


            option.dataset.available =
                book.available_quantity;


            select.appendChild(option);

        });


        if (availableBooks.length === 0) {

            select.innerHTML =
                `<option value="">
                    No books available
                 </option>`;

        }


        updateBookAvailability();


    } catch (error) {

        console.error(error);

        select.innerHTML =
            `<option value="">
                Unable to load books
             </option>`;

    }

}


// =========================================================
// LOAD MEMBERS FOR ISSUE
// =========================================================

async function loadIssueMembers() {

    const select =
        document.getElementById(
            "issueMember"
        );


    if (!select) {
        return;
    }


    try {

        const response =
            await fetch(MEMBER_API);


        const members =
            await response.json();


        select.innerHTML =
            `<option value="">
                Select a member
             </option>`;


        members.forEach(member => {

            const option =
                document.createElement(
                    "option"
                );


            option.value =
                member.id;


            option.textContent =
                `${member.name} — ${member.phone || "No phone"}`;


            option.dataset.name =
                member.name;


            select.appendChild(option);

        });


        if (members.length === 0) {

            select.innerHTML =
                `<option value="">
                    No members found
                 </option>`;

        }


        updateIssueSummary();


    } catch (error) {

        console.error(error);

        select.innerHTML =
            `<option value="">
                Unable to load members
             </option>`;

    }

}


// =========================================================
// ISSUE DATES
// =========================================================

function setDefaultIssueDates() {

    const issueDate =
        document.getElementById(
            "issueDate"
        );


    const dueDate =
        document.getElementById(
            "dueDate"
        );


    if (!issueDate ||
        !dueDate) {

        return;

    }


    const today =
        new Date();


    const todayString =
        today.toISOString()
            .split("T")[0];


    issueDate.value =
        todayString;


    issueDate.min =
        todayString;


    const due =
        new Date(today);


    due.setDate(
        due.getDate() + 14
    );


    dueDate.value =
        due.toISOString()
            .split("T")[0];


    dueDate.min =
        todayString;

}


// =========================================================
// BOOK AVAILABILITY
// =========================================================

function updateBookAvailability() {

    const select =
        document.getElementById(
            "issueBook"
        );


    const display =
        document.getElementById(
            "bookAvailability"
        );


    if (!select ||
        !display) {

        return;

    }


    const option =
        select.options[
            select.selectedIndex
        ];


    if (!option ||
        !option.value) {

        display.textContent =
            "Select a book";

        return;

    }


    display.textContent =
        `${option.dataset.available} copies available`;

}


// =========================================================
// ISSUE SUMMARY
// =========================================================

function updateIssueSummary() {

    const bookSelect =
        document.getElementById(
            "issueBook"
        );


    const memberSelect =
        document.getElementById(
            "issueMember"
        );


    const dueDate =
        document.getElementById(
            "dueDate"
        );


    if (!bookSelect ||
        !memberSelect) {

        return;

    }


    const bookOption =
        bookSelect.options[
            bookSelect.selectedIndex
        ];


    const memberOption =
        memberSelect.options[
            memberSelect.selectedIndex
        ];


    document.getElementById(
        "summaryBook"
    ).textContent =

        bookOption &&
        bookOption.value

            ? bookOption.dataset.title

            : "-";


    document.getElementById(
        "summaryMember"
    ).textContent =

        memberOption &&
        memberOption.value

            ? memberOption.dataset.name

            : "-";


    document.getElementById(
        "summaryDueDate"
    ).textContent =

        dueDate &&
        dueDate.value

            ? dueDate.value

            : "-";

}


// =========================================================
// ISSUE SELECT EVENTS
// =========================================================

document
    .getElementById("issueBook")
    ?.addEventListener(
        "change",
        function() {

            updateBookAvailability();

            updateIssueSummary();

        }
    );


document
    .getElementById("issueMember")
    ?.addEventListener(
        "change",
        function() {

            updateIssueSummary();

        }
    );


document
    .getElementById("dueDate")
    ?.addEventListener(
        "change",
        function() {

            updateIssueSummary();

        }
    );


// =========================================================
// ISSUE BOOK
// =========================================================

document
    .getElementById("issueForm")
    ?.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const bookId =
                document.getElementById(
                    "issueBook"
                ).value;


            const memberId =
                document.getElementById(
                    "issueMember"
                ).value;


            const issueDate =
                document.getElementById(
                    "issueDate"
                ).value;


            const dueDate =
                document.getElementById(
                    "dueDate"
                ).value;


            if (!bookId) {

                showToast(
                    "Please select a book",
                    "error"
                );

                return;

            }


            if (!memberId) {

                showToast(
                    "Please select a member",
                    "error"
                );

                return;

            }


            if (dueDate < issueDate) {

                showToast(
                    "Due date cannot be before issue date",
                    "error"
                );

                return;

            }


            const formData =
                new URLSearchParams();


            formData.append(
                "book_id",
                bookId
            );


            formData.append(
                "member_id",
                memberId
            );


            formData.append(
                "issue_date",
                issueDate
            );


            formData.append(
                "due_date",
                dueDate
            );


            const button =
                document.getElementById(
                    "issueSubmitBtn"
                );


            button.disabled = true;

            button.textContent =
                "Issuing...";


            try {

                const response =
                    await fetch(
                        ISSUE_API,
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/x-www-form-urlencoded"
                            },

                            body: formData
                        }
                    );


                const result =
                    await response.json();


                if (!response.ok ||
                    !result.success) {

                    throw new Error(
                        result.message ||
                        "Unable to issue book"
                    );

                }


                showToast(
                    result.message ||
                    "Book issued successfully"
                );


                resetIssueForm();


                await loadDashboard();

                await loadIssueBooks();


            } catch (error) {

                console.error(error);

                showToast(
                    error.message,
                    "error"
                );


            } finally {

                button.disabled = false;

                button.textContent =
                    "Issue Book";

            }

        }
    );


// =========================================================
// RESET ISSUE
// =========================================================

function resetIssueForm() {

    const form =
        document.getElementById(
            "issueForm"
        );


    if (!form) {
        return;
    }


    form.reset();


    setDefaultIssueDates();

    updateBookAvailability();

    updateIssueSummary();

}


// =========================================================
// RETURN BOOKS
// =========================================================

async function loadReturns() {

    const table =
        document.getElementById(
            "returnTableBody"
        );


    if (!table) {
        return;
    }


    try {

        const search =
            document.getElementById(
                "returnSearch"
            )?.value || "";


        let url =
            RETURN_API;


        if (search.trim()) {

            url +=
                "?search=" +
                encodeURIComponent(
                    search.trim()
                );

        }


        const response =
            await fetch(url);


        if (!response.ok) {

            throw new Error(
                "Unable to load issued books"
            );

        }


        const issues =
            await response.json();


        table.innerHTML = "";


        if (issues.length === 0) {

            table.innerHTML = `

                <tr>

                    <td
                        colspan="8"
                        class="empty-message">

                        No books are currently issued.

                    </td>

                </tr>

            `;

            return;

        }


        issues.forEach(issue => {

            const row =
                document.createElement(
                    "tr"
                );


            const lateDays =
                Number(
                    issue.late_days || 0
                );


            const fine =
                Number(
                    issue.fine || 0
                );


            row.innerHTML = `

                <td>

                    <strong>
                        ${escapeHtml(
                            issue.title
                        )}
                    </strong>

                </td>


                <td>
                    ${escapeHtml(
                        issue.isbn || "-"
                    )}
                </td>


                <td>
                    ${escapeHtml(
                        issue.member_name || "-"
                    )}
                </td>


                <td>
                    ${escapeHtml(
                        issue.issue_date || "-"
                    )}
                </td>


                <td
                    class="${
                        lateDays > 0
                            ? "late-text"
                            : ""
                    }">

                    ${escapeHtml(
                        issue.due_date || "-"
                    )}

                </td>


                <td
                    class="${
                        lateDays > 0
                            ? "late-text"
                            : ""
                    }">

                    ${lateDays}

                </td>


                <td
                    class="${
                        fine > 0
                            ? "late-text"
                            : ""
                    }">

                    ₹${fine.toFixed(2)}

                </td>


                <td>

                    <button
                        class="return-btn"
                        onclick="returnBook(${issue.id})">

                        Return

                    </button>

                </td>

            `;


            table.appendChild(row);

        });


    } catch (error) {

        console.error(error);

        table.innerHTML = `

            <tr>

                <td
                    colspan="8"
                    class="empty-message">

                    Unable to load issued books.

                </td>

            </tr>

        `;

    }

}


// =========================================================
// RETURN BOOK
// =========================================================

async function returnBook(issueId) {

    if (!confirm(
        "Are you sure you want to return this book?"
    )) {

        return;

    }


    const formData =
        new URLSearchParams();


    formData.append(
        "issue_id",
        issueId
    );


    try {

        const response =
            await fetch(
                RETURN_API,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body: formData
                }
            );


        const result =
            await response.json();


        if (!response.ok ||
            !result.success) {

            throw new Error(
                result.message ||
                "Unable to return book"
            );

        }


        showToast(
            result.message ||
            "Book returned successfully"
        );


        await loadReturns();

        await loadDashboard();


    } catch (error) {

        console.error(error);

        showToast(
            error.message,
            "error"
        );

    }

}


// =========================================================
// INITIAL LOAD
// =========================================================

document.addEventListener(
    "DOMContentLoaded",
    function() {

        loadDashboard();

    }
);