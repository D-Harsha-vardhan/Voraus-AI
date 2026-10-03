import { Controller, Get, Post, Delete, Param, Req, Body, UseInterceptors, UploadedFile } from '@nestjs/common';
import { DocumentsService } from './documents.service';
// import { FileInterceptor } from '@nestjs/platform-express';

@Controller('documents')
export class DocumentsController {
  constructor(private readonly documentsService: DocumentsService) {}

  @Get()
  async getDocuments(@Req() req: any) {
    const applicantId = req.headers['x-applicant-id'];
    return this.documentsService.getDocuments(applicantId);
  }

  @Get(':id')
  async getDocument(@Param('id') id: string) {
    return this.documentsService.getDocument(id);
  }

  // Uses multer in a real environment for multipart/form-data
  @Post('upload')
  // @UseInterceptors(FileInterceptor('file'))
  async uploadDocument(
    @Req() req: any,
    @Body() body: any,
    // @UploadedFile() file: Express.Multer.File,
  ) {
    const applicantId = req.headers['x-applicant-id'];
    const { documentType } = body;
    // Mocking file upload to Supabase Storage
    return this.documentsService.uploadDocument(applicantId, documentType, 'mocked-file-metadata');
  }

  @Post(':id/process')
  async processDocument(@Param('id') id: string) {
    return this.documentsService.processDocument(id);
  }

  @Delete(':id')
  async deleteDocument(@Param('id') id: string) {
    return this.documentsService.deleteDocument(id);
  }
}
