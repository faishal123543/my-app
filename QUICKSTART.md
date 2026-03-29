# Loan Management Dashboard - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Prerequisites
- Java 17+ installed
- Node.js 16+ installed  
- PostgreSQL 12+ running on `localhost:5432`
- Angular CLI (for development)

### Step 1: Start the Backend Server

```bash
# Navigate to backend directory
cd C:\Users\faishal\loanManagementApp

# Clean and compile
mvn clean compile

# Start Spring Boot server
mvn spring-boot:run

# Server will start on http://localhost:8080
```

**Expected Output**:
```
Started LoanManagementAppApplication in X seconds
Listening on port 8080
```

### Step 2: Start the Angular Frontend

```bash
# Navigate to frontend directory
cd C:\Users\faishal\loanManagementApp-frontend

# Install dependencies (if not done)
npm install

# Start development server
ng serve

# or
npm start

# App will be available at http://localhost:4200
```

**Expected Output**:
```
✔ Compiled successfully.
✔ Built successfully.
Application bundle generated successfully.
```

### Step 3: Test the Application

1. **Open Browser**: Navigate to `http://localhost:4200`
2. **Login**: 
   - Mobile Number: Your registered mobile
   - Password: Your password
3. **View Dashboard**: After login, you should see the dashboard with statistics
4. **Apply for Loan**: Click "Apply Loan" button to test the form

---

## 📱 Testing Credentials

```
Mobile Number: 0512345678
Password: password123
```

(Create new account via registration if needed)

---

## ✨ Features Implemented

### Dashboard
- ✅ User profile summary (Name, Mobile, National ID, Status)
- ✅ Statistics cards (Total, Approved, Pending, Rejected)
- ✅ Loan amount summary (Requested, Approved, Remaining)
- ✅ Recent loan applications table
- ✅ Responsive design
- ✅ Error handling with user-friendly messages
- ✅ Loading states

### Apply Loan Form
- ✅ Multi-step form layout
- ✅ Loan type selection (Personal, Car, Home)
- ✅ Real-time validation
- ✅ Financial information fields
- ✅ Loan summary calculation
- ✅ Error messages
- ✅ Success notifications
- ✅ Auto-redirect after submission

### Backend APIs
- ✅ POST `/api/v1/loans` - Apply for loan
- ✅ GET `/api/v1/loans?userId=X` - Get user's loans
- ✅ GET `/api/v1/loans/dashboard?userId=X` - Get dashboard data
- ✅ GET `/api/v1/loans/{id}?userId=X` - Get specific loan
- ✅ PUT `/api/v1/loans/{id}/status` - Update loan status (admin)

### Business Logic
- ✅ Credit score validation (minimum 600)
- ✅ Monthly income validation (minimum 3,000)
- ✅ Debt-to-income ratio check (max 50%)
- ✅ Prevent duplicate active loans
- ✅ Auto-calculate EMI

---

## 🎯 Key Functionality

### Dashboard Overview
```
┌─────────────────────────────────────────┐
│  Welcome, Ahmed Al-Dosari!              │
│  [+ Apply Loan]  [🔄 Refresh]           │
└─────────────────────────────────────────┘

┌──────────────┬──────────────┬──────────────┬──────────────┐
│  📊 Total    │  ✅ Approved │  ⏳ Pending  │  ❌ Rejected │
│      3       │      1       │      1       │      1       │
└──────────────┴──────────────┴──────────────┴──────────────┘

┌──────────────────────────────────────────────────────────┐
│  Loan Amount Summary                                     │
│  Total Requested: SAR 200,000                            │
│  Total Approved:  SAR 50,000                             │
│  Remaining:       SAR 50,000                             │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  Recent Loan Applications                                │
│  ┌─────┬──────────┬──────────┬─────────┬────────┬────────┐
│  │ ID  │ Type     │ Amount   │ Status  │ Date   │ Actions│
│  ├─────┼──────────┼──────────┼─────────┼────────┼────────┤
│  │ #42 │ HOME     │ 500,000  │ PENDING │ 3/15   │  👁️   │
│  │ #41 │ CAR      │ 80,000   │ PENDING │ 3/10   │  👁️   │
│  └─────┴──────────┴──────────┴─────────┴────────┴────────┘
└──────────────────────────────────────────────────────────┘
```

---

## 🔍 API Response Examples

### Successful Loan Application
```javascript
// POST /api/v1/loans?userId=1
// Request:
{
  "loanType": "HOME",
  "amount": 500000,
  "durationMonths": 240,
  "monthlyIncome": 35000,
  "employerName": "Al Rajhi Bank",
  "hasExistingLoans": false,
  "creditScore": 750,
  "purposeOfLoan": "Purchasing a house"
}

// Response (201 Created):
{
  "id": 42,
  "loanType": "HOME",
  "amount": 500000,
  "status": "PENDING",
  "durationMonths": 240,
  "monthlyIncome": 35000,
  "employerName": "Al Rajhi Bank",
  "hasExistingLoans": false,
  "creditScore": 750,
  "purposeOfLoan": "Purchasing a house",
  "createdAt": "2024-03-21T14:30:00",
  "updatedAt": "2024-03-21T14:30:00"
}
```

### Dashboard Summary
```javascript
// GET /api/v1/loans/dashboard?userId=1
// Response (200 OK):
{
  "totalLoansApplied": 3,
  "approvedLoansCount": 1,
  "rejectedLoansCount": 1,
  "pendingLoansCount": 1,
  "totalLoanAmountRequested": 200000,
  "totalApprovedAmount": 50000,
  "remainingBalance": 50000,
  "userName": "Ahmed Al-Dosari",
  "mobileNumber": "+966512345678",
  "nationalId": "1234567890",
  "accountStatus": "ACTIVE"
}
```

---

## 🛠️ Troubleshooting

| Issue | Solution |
|-------|----------|
| Backend won't start | Check PostgreSQL is running, port 8080 is free |
| Frontend won't compile | Run `npm install`, delete `node_modules`, reinstall |
| CORS error | Ensure backend CORS config includes localhost:4200 |
| 401 Unauthorized | Make sure you're logged in, userId is in parameters |
| Dashboard shows "No Data" | Create a test loan application first |
| Form validation not showing | Click a field and click away to trigger validation |

---

## 📖 File Structure

```
Backend:
loanManagementApp/
├── src/main/java/com/loanmanagement/app/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Loan.java          ← NEW
│   │   └── dto/
│   │       ├── LoanRequest.java        ← NEW
│   │       ├── LoanResponse.java       ← NEW
│   │       ├── DashboardSummary.java   ← NEW
│   │       └── AuthResponse.java       (UPDATED)
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── LoanRepository.java         ← NEW
│   ├── service/
│   │   ├── AuthService.java
│   │   └── LoanService.java            ← NEW
│   ├── controller/
│   │   ├── AuthController.java         (UPDATED)
│   │   └── LoanController.java         ← NEW
│   └── exception/
│       └── LoanApplicationException.java ← NEW

Frontend:
loanManagementApp-frontend/
└── src/app/
    ├── core/services/
    │   └── loan.service.ts             ← NEW
    └── features/dashboard/
        └── components/
            ├── dashboard/
            │   ├── dashboard.component.ts     (UPDATED)
            │   ├── dashboard.component.html   (UPDATED)
            │   └── dashboard.component.css    (UPDATED)
            └── apply-loan/
                ├── apply-loan.component.ts    ← NEW
                ├── apply-loan.component.html  ← NEW
                └── apply-loan.component.css   ← NEW
```

---

## 🔑 Environment Variables (Optional)

Create `.env` file in project root:

```properties
# Backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/loan_management
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# Frontend
ANGULAR_API_URL=http://localhost:8080/api/v1
```

---

## 📚 Documentation

- **Full Implementation Guide**: `DASHBOARD_IMPLEMENTATION.md`
- **API Documentation**: See endpoint specifications in LoanController.java
- **Form Validation**: See validators in LoanRequest.java
- **Business Logic**: See LoanService.java for rules

---

## ✅ Verification Checklist

- [ ] Backend compiles without errors: `mvn clean compile`
- [ ] Backend starts successfully: `mvn spring-boot:run`
- [ ] Frontend dependencies installed: `npm install`
- [ ] Frontend builds successfully: `ng build`
- [ ] Frontend dev server starts: `ng serve`
- [ ] Can access dashboard at http://localhost:4200
- [ ] Dashboard loads user data
- [ ] Can navigate to apply loan form
- [ ] Form validation works
- [ ] Can submit loan application
- [ ] New loan appears in recent applications

---

## 🎓 Learning Path

1. **Understand the Architecture**: Read DASHBOARD_IMPLEMENTATION.md
2. **Start Backend**: Run `mvn spring-boot:run`
3. **Start Frontend**: Run `ng serve`
4. **Test APIs**: Use Postman to test endpoints  
5. **Test UI**: Interact with dashboard and forms
6. **Review Code**: Study LoanService.java and dashboard.component.ts
7. **Extend Features**: Add your own enhancements

---

## 🚀 Next Steps

1. **Configure Real SMS**: Update SMS provider in AuthService
2. **Add JWT Tokens**: Implement proper JWT authentication
3. **Admin Dashboard**: Create admin panel for loan approvals
4. **Notifications**: Add email/SMS notifications
5. **Document Upload**: Allow users to upload documents
6. **Payment Tracking**: Implement payment recording system
7. **Tests**: Add unit and integration tests

---

## 💬 Quick Tips

- Use browser DevTools to inspect API calls
- Check browser console for error messages
- Check backend logs for validation errors
- Test with different credit scores to see rejection
- Try different loan types to test form behavior
- Use the "Refresh" button on dashboard to reload data

---

**Happy Coding! 🎉**

For detailed information, see `DASHBOARD_IMPLEMENTATION.md`
