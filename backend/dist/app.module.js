"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AppModule = void 0;
const common_1 = require("@nestjs/common");
const app_controller_js_1 = require("./app.controller.js");
const app_service_js_1 = require("./app.service.js");
const auth_module_js_1 = require("./auth/auth.module.js");
const applicant_module_js_1 = require("./applicant/applicant.module.js");
const prisma_module_js_1 = require("./prisma/prisma.module.js");
const documents_module_js_1 = require("./documents/documents.module.js");
const ai_module_js_1 = require("./ai/ai.module.js");
const qualification_module_js_1 = require("./qualification/qualification.module.js");
const opportunities_module_js_1 = require("./opportunities/opportunities.module.js");
const webhooks_module_js_1 = require("./webhooks/webhooks.module.js");
let AppModule = class AppModule {
};
exports.AppModule = AppModule;
exports.AppModule = AppModule = __decorate([
    (0, common_1.Module)({
        imports: [auth_module_js_1.AuthModule, applicant_module_js_1.ApplicantModule, prisma_module_js_1.PrismaModule, documents_module_js_1.DocumentsModule, ai_module_js_1.AiModule, qualification_module_js_1.QualificationModule, opportunities_module_js_1.OpportunitiesModule, webhooks_module_js_1.WebhooksModule],
        controllers: [app_controller_js_1.AppController],
        providers: [app_service_js_1.AppService],
    })
], AppModule);
//# sourceMappingURL=app.module.js.map