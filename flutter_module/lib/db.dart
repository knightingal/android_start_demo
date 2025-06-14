import 'package:flutter/material.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

class DB {
  late final Future<Database> database;

  void init() {
    WidgetsFlutterBinding.ensureInitialized();
    database = getDatabasesPath().then(
      (basePath) => openDatabase(join(basePath, 'database-flow1000')),
    );
  }

  Future<List<Map<String, Object?>>> queryDb() {
    return database.then((db) {
      return db.query(
        "PicSectionBean",
        columns: ["id", "name"],
        where: "id=?",
        whereArgs: [1],
      );
    });
  }
}
